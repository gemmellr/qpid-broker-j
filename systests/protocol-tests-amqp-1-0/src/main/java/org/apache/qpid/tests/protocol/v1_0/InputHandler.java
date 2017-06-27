/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.qpid.tests.protocol.v1_0;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.qpid.server.bytebuffer.QpidByteBuffer;
import org.apache.qpid.server.protocol.v1_0.ConnectionHandler;
import org.apache.qpid.server.protocol.v1_0.codec.ValueHandler;
import org.apache.qpid.server.protocol.v1_0.framing.FrameHandler;
import org.apache.qpid.server.protocol.v1_0.type.FrameBody;
import org.apache.qpid.server.protocol.v1_0.type.SaslFrameBody;
import org.apache.qpid.server.protocol.v1_0.type.UnsignedShort;
import org.apache.qpid.server.protocol.v1_0.type.codec.AMQPDescribedTypeRegistry;
import org.apache.qpid.server.protocol.v1_0.type.security.SaslChallenge;
import org.apache.qpid.server.protocol.v1_0.type.security.SaslCode;
import org.apache.qpid.server.protocol.v1_0.type.security.SaslInit;
import org.apache.qpid.server.protocol.v1_0.type.security.SaslMechanisms;
import org.apache.qpid.server.protocol.v1_0.type.security.SaslOutcome;
import org.apache.qpid.server.protocol.v1_0.type.security.SaslResponse;
import org.apache.qpid.server.protocol.v1_0.type.transport.Attach;
import org.apache.qpid.server.protocol.v1_0.type.transport.Begin;
import org.apache.qpid.server.protocol.v1_0.type.transport.ChannelFrameBody;
import org.apache.qpid.server.protocol.v1_0.type.transport.Close;
import org.apache.qpid.server.protocol.v1_0.type.transport.Detach;
import org.apache.qpid.server.protocol.v1_0.type.transport.Disposition;
import org.apache.qpid.server.protocol.v1_0.type.transport.End;
import org.apache.qpid.server.protocol.v1_0.type.transport.Error;
import org.apache.qpid.server.protocol.v1_0.type.transport.Flow;
import org.apache.qpid.server.protocol.v1_0.type.transport.Open;
import org.apache.qpid.server.protocol.v1_0.type.transport.Transfer;

public class InputHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(InputHandler.class);
    private static final AMQPDescribedTypeRegistry TYPE_REGISTRY = AMQPDescribedTypeRegistry.newInstance()
                                                                                            .registerTransportLayer()
                                                                                            .registerMessagingLayer()
                                                                                            .registerTransactionLayer()
                                                                                            .registerSecurityLayer()
                                                                                            .registerExtensionSoleconnLayer();

    private enum ParsingState
    {
        HEADER,
        PERFORMATIVES
    };

    private final MyConnectionHandler _connectionHandler;
    private final ValueHandler _valueHandler;
    private final BlockingQueue<Response<?>> _responseQueue;

    private QpidByteBuffer _inputBuffer = QpidByteBuffer.allocate(0);
    private volatile FrameHandler _frameHandler;
    private volatile ParsingState _state = ParsingState.HEADER;

    public InputHandler(final BlockingQueue<Response<?>> queue, final boolean isSasl)
    {

        _valueHandler = new ValueHandler(TYPE_REGISTRY);
        _connectionHandler = new MyConnectionHandler();
        _frameHandler = new FrameHandler(_valueHandler, _connectionHandler, isSasl);

        _responseQueue = queue;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception
    {
        // TODO does Netty take care of saving the remaining bytes???
        ByteBuf buf = (ByteBuf) msg;
        QpidByteBuffer qpidBuf = QpidByteBuffer.wrap(buf.nioBuffer());

        if (_inputBuffer.hasRemaining())
        {
            QpidByteBuffer old = _inputBuffer;
            _inputBuffer = QpidByteBuffer.allocate(_inputBuffer.remaining() + qpidBuf.remaining());
            _inputBuffer.put(old);
            _inputBuffer.put(qpidBuf);
            old.dispose();
            qpidBuf.dispose();
            _inputBuffer.flip();
        }
        else
        {
            _inputBuffer.dispose();
            _inputBuffer = qpidBuf;
        }

        doParsing();

        if (_inputBuffer.hasRemaining())
        {
            _inputBuffer.compact();
        }

        ReferenceCountUtil.release(msg);
    }

    private void doParsing()
    {
        switch(_state)
        {
            case HEADER:
                if (_inputBuffer.remaining() >= 8)
                {
                    byte[] header = new byte[8];
                    _inputBuffer.get(header);
                    _responseQueue.add(new HeaderResponse(header));
                    _state = ParsingState.PERFORMATIVES;
                    doParsing();
                }
                break;
            case PERFORMATIVES:
                _frameHandler.parse(_inputBuffer);
                break;
            default:
                throw new IllegalStateException("Unexpected state : " + _state);
        }
    }

    private void resetInputHandlerAfterSaslOutcome()
    {
        _state = ParsingState.HEADER;
        _frameHandler = new FrameHandler(_valueHandler, _connectionHandler, false);
    }

    private class MyConnectionHandler implements ConnectionHandler
    {
        @Override
        public void receiveOpen(final int channel, final Open close)
        {
        }

        @Override
        public void receiveClose(final int channel, final Close close)
        {

        }

        @Override
        public void receiveBegin(final int channel, final Begin begin)
        {

        }

        @Override
        public void receiveEnd(final int channel, final End end)
        {

        }

        @Override
        public void receiveAttach(final int channel, final Attach attach)
        {

        }

        @Override
        public void receiveDetach(final int channel, final Detach detach)
        {

        }

        @Override
        public void receiveTransfer(final int channel, final Transfer transfer)
        {

        }

        @Override
        public void receiveDisposition(final int channel, final Disposition disposition)
        {

        }

        @Override
        public void receiveFlow(final int channel, final Flow flow)
        {

        }

        @Override
        public int getMaxFrameSize()
        {
            return 512;
        }

        @Override
        public int getChannelMax()
        {
            return UnsignedShort.MAX_VALUE.intValue();
        }

        @Override
        public void handleError(final Error parsingError)
        {
            LOGGER.error("Unexpected error {}", parsingError);
        }

        @Override
        public boolean closedForInput()
        {
            return false;
        }

        @Override
        public void receive(final List<ChannelFrameBody> channelFrameBodies)
        {
            for (final ChannelFrameBody channelFrameBody : channelFrameBodies)
            {
                Response response;
                Object val = channelFrameBody.getFrameBody();
                int channel = channelFrameBody.getChannel();
                if (val instanceof FrameBody)
                {
                    FrameBody frameBody = (FrameBody) val;
                    response = new PerformativeResponse((short) channel, frameBody);
                }
                else if (val instanceof SaslFrameBody)
                {
                    SaslFrameBody frameBody = (SaslFrameBody) val;
                    response = new SaslPerformativeResponse((short) channel, frameBody);

                    if (frameBody instanceof SaslOutcome && ((SaslOutcome) frameBody).getCode().equals(SaslCode.OK))
                    {
                        resetInputHandlerAfterSaslOutcome();
                    }
                }
                else
                {
                    throw new UnsupportedOperationException("Unexpected frame type : " + val.getClass());
                }

                _responseQueue.add(response);
            }
        }

        @Override
        public void receiveSaslInit(final SaslInit saslInit)
        {

        }

        @Override
        public void receiveSaslMechanisms(final SaslMechanisms saslMechanisms)
        {

        }

        @Override
        public void receiveSaslChallenge(final SaslChallenge saslChallenge)
        {

        }

        @Override
        public void receiveSaslResponse(final SaslResponse saslResponse)
        {

        }

        @Override
        public void receiveSaslOutcome(final SaslOutcome saslOutcome)
        {

        }
    }
}