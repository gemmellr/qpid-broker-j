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
 *
 */

package org.apache.qpid.server.protocol.v1_0;

import java.util.List;

import org.apache.qpid.bytebuffer.QpidByteBuffer;
import org.apache.qpid.server.logging.EventLoggerProvider;
import org.apache.qpid.server.model.ManagedObject;
import org.apache.qpid.server.protocol.v1_0.codec.SectionDecoderRegistry;
import org.apache.qpid.server.protocol.v1_0.type.FrameBody;
import org.apache.qpid.server.protocol.v1_0.type.codec.AMQPDescribedTypeRegistry;
import org.apache.qpid.server.protocol.v1_0.type.transport.End;
import org.apache.qpid.server.protocol.v1_0.type.transport.Error;
import org.apache.qpid.server.transport.AMQPConnection;
import org.apache.qpid.server.transport.ProtocolEngine;

@ManagedObject(category = false, creatable = false, type="AMQP_1_0")
public interface AMQPConnection_1_0<C extends AMQPConnection_1_0<C>> extends AMQPConnection<C>,
                                                                             ProtocolEngine, EventLoggerProvider
{
    Object getReference();

    String getRemoteContainerId();

    SectionDecoderRegistry getSectionDecoderRegistry();

    AMQPDescribedTypeRegistry getDescribedTypeRegistry();

    int sendFrame(short channel, FrameBody body, List<QpidByteBuffer> payload);

    void sendFrame(short channel, FrameBody body);

    void sendEnd(short sendChannel, End end, boolean b);

    void sessionEnded(Session_1_0 session_1_0);

    boolean isClosed();

    void close(Error error);
}
