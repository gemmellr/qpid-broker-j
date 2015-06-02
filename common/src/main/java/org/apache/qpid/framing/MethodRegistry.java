/*
 *
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

/*
 * This file is auto-generated by Qpid Gentools v.0.1 - do not modify.
 * Supported AMQP version:
  *   0-9
  *   0-91
  *   8-0
  */

package org.apache.qpid.framing;

import java.util.Map;

public final class MethodRegistry
{
    private ProtocolVersion _protocolVersion;


    public MethodRegistry(ProtocolVersion pv)
    {
        _protocolVersion = pv;
    }

    public void setProtocolVersion(final ProtocolVersion protocolVersion)
    {
        _protocolVersion = protocolVersion;
    }

    public final AccessRequestBody createAccessRequestBody(final AMQShortString realm,
                                                           final boolean exclusive,
                                                           final boolean passive,
                                                           final boolean active,
                                                           final boolean write,
                                                           final boolean read)
    {
        return new AccessRequestBody(realm,
                                     exclusive,
                                     passive,
                                     active,
                                     write,
                                     read);
    }

    public final AccessRequestOkBody createAccessRequestOkBody(final int ticket)
    {
        return new AccessRequestOkBody(ticket);
    }


    public final BasicQosBody createBasicQosBody(final long prefetchSize,
                                                 final int prefetchCount,
                                                 final boolean global)
    {
        return new BasicQosBody(prefetchSize,
                                prefetchCount,
                                global);
    }

    public final BasicQosOkBody createBasicQosOkBody()
    {
        return new BasicQosOkBody();
    }

    public final BasicConsumeBody createBasicConsumeBody(final int ticket,
                                                         final String queue,
                                                         final String consumerTag,
                                                         final boolean noLocal,
                                                         final boolean noAck,
                                                         final boolean exclusive,
                                                         final boolean nowait,
                                                         final Map<String,Object> arguments)
    {

        return new BasicConsumeBody(ticket,
                                    AMQShortString.valueOf(queue),
                                    AMQShortString.valueOf(consumerTag),
                                    noLocal,
                                    noAck,
                                    exclusive,
                                    nowait,
                                    FieldTable.convertToFieldTable(arguments));
    }

    public final BasicConsumeOkBody createBasicConsumeOkBody(final AMQShortString consumerTag)
    {
        return new BasicConsumeOkBody(consumerTag);
    }

    public final BasicCancelBody createBasicCancelBody(final AMQShortString consumerTag,
                                                       final boolean nowait)
    {
        return new BasicCancelBody(consumerTag,
                                   nowait);
    }

    public final BasicCancelOkBody createBasicCancelOkBody(final AMQShortString consumerTag)
    {
        return new BasicCancelOkBody(consumerTag);
    }

    public final BasicPublishBody createBasicPublishBody(final int ticket,
                                                         final String exchange,
                                                         final String routingKey,
                                                         final boolean mandatory,
                                                         final boolean immediate)
    {
        return new BasicPublishBody(ticket,
                                    AMQShortString.valueOf(exchange),
                                    AMQShortString.valueOf(routingKey),
                                    mandatory,
                                    immediate);
    }

    public final BasicReturnBody createBasicReturnBody(final int replyCode,
                                                       final AMQShortString replyText,
                                                       final AMQShortString exchange,
                                                       final AMQShortString routingKey)
    {
        return new BasicReturnBody(replyCode,
                                   replyText,
                                   exchange,
                                   routingKey);
    }

    public final BasicDeliverBody createBasicDeliverBody(final AMQShortString consumerTag,
                                                         final long deliveryTag,
                                                         final boolean redelivered,
                                                         final AMQShortString exchange,
                                                         final AMQShortString routingKey)
    {
        return new BasicDeliverBody(consumerTag,
                                    deliveryTag,
                                    redelivered,
                                    exchange,
                                    routingKey);
    }

    public final BasicGetBody createBasicGetBody(final int ticket,
                                                 final AMQShortString queue,
                                                 final boolean noAck)
    {
        return new BasicGetBody(ticket,
                                queue,
                                noAck);
    }

    public final BasicGetOkBody createBasicGetOkBody(final long deliveryTag,
                                                     final boolean redelivered,
                                                     final AMQShortString exchange,
                                                     final AMQShortString routingKey,
                                                     final long messageCount)
    {
        return new BasicGetOkBody(deliveryTag,
                                  redelivered,
                                  exchange,
                                  routingKey,
                                  messageCount);
    }

    public final BasicGetEmptyBody createBasicGetEmptyBody(final AMQShortString clusterId)
    {
        return new BasicGetEmptyBody(clusterId);
    }

    public final BasicAckBody createBasicAckBody(final long deliveryTag,
                                                 final boolean multiple)
    {
        return new BasicAckBody(deliveryTag,
                                multiple);
    }

    public final BasicRejectBody createBasicRejectBody(final long deliveryTag,
                                                       final boolean requeue)
    {
        return new BasicRejectBody(deliveryTag,
                                   requeue);
    }

    public final BasicRecoverBody createBasicRecoverBody(final boolean requeue)
    {
        return new BasicRecoverBody(requeue);
    }


    public final BasicRecoverSyncOkBody createBasicRecoverSyncOkBody()
    {
        return new BasicRecoverSyncOkBody(_protocolVersion);
    }


    public final BasicRecoverSyncBody createBasicRecoverSyncBody(final boolean requeue)
    {
        return new BasicRecoverSyncBody(_protocolVersion, requeue);
    }

    public final ChannelAlertBody createChannelAlertBody(final int replyCode,
                                                         final AMQShortString replyText,
                                                         final FieldTable details)
    {
        return new ChannelAlertBody(replyCode,
                                    replyText,
                                    details);
    }

    public final ChannelOpenBody createChannelOpenBody(final AMQShortString outOfBand)
    {
        return new ChannelOpenBody();
    }

    public final ChannelOpenOkBody createChannelOpenOkBody(byte[] channelId)
    {
        return createChannelOpenOkBody();
    }

    public final ChannelOpenOkBody createChannelOpenOkBody()
    {
        return _protocolVersion.equals(ProtocolVersion.v0_8)
                ? ChannelOpenOkBody.INSTANCE_0_8
                : ChannelOpenOkBody.INSTANCE_0_9;
    }

    public final ChannelFlowBody createChannelFlowBody(final boolean active)
    {
        return new ChannelFlowBody(active);
    }

    public final ChannelFlowOkBody createChannelFlowOkBody(final boolean active)
    {
        return new ChannelFlowOkBody(active);
    }

    public final ChannelCloseBody createChannelCloseBody(final int replyCode, final AMQShortString replyText,
                                                         final int classId,
                                                         final int methodId
                                                        )
    {
        return new ChannelCloseBody(replyCode,
                                    replyText,
                                    classId,
                                    methodId);
    }

    public final ChannelCloseOkBody createChannelCloseOkBody()
    {
        return ChannelCloseOkBody.INSTANCE;
    }




    public final ConnectionStartBody createConnectionStartBody(final short versionMajor,
                                                               final short versionMinor,
                                                               final FieldTable serverProperties,
                                                               final byte[] mechanisms,
                                                               final byte[] locales)
    {
        return new ConnectionStartBody(versionMajor,
                                       versionMinor,
                                       serverProperties,
                                       mechanisms,
                                       locales);
    }

    public final ConnectionStartOkBody createConnectionStartOkBody(final FieldTable clientProperties,
                                                                   final AMQShortString mechanism,
                                                                   final byte[] response,
                                                                   final AMQShortString locale)
    {
        return new ConnectionStartOkBody(clientProperties,
                                         mechanism,
                                         response,
                                         locale);
    }

    public final ConnectionSecureBody createConnectionSecureBody(final byte[] challenge)
    {
        return new ConnectionSecureBody(challenge);
    }

    public final ConnectionSecureOkBody createConnectionSecureOkBody(final byte[] response)
    {
        return new ConnectionSecureOkBody(response);
    }

    public final ConnectionTuneBody createConnectionTuneBody(final int channelMax,
                                                             final long frameMax,
                                                             final int heartbeat)
    {
        return new ConnectionTuneBody(channelMax,
                                      frameMax,
                                      heartbeat);
    }

    public final ConnectionTuneOkBody createConnectionTuneOkBody(final int channelMax,
                                                                 final long frameMax,
                                                                 final int heartbeat)
    {
        return new ConnectionTuneOkBody(channelMax,
                                        frameMax,
                                        heartbeat);
    }

    public final ConnectionOpenBody createConnectionOpenBody(final AMQShortString virtualHost,
                                                             final AMQShortString capabilities,
                                                             final boolean insist)
    {
        return new ConnectionOpenBody(virtualHost,
                                      capabilities,
                                      insist);
    }

    public final ConnectionOpenOkBody createConnectionOpenOkBody(final AMQShortString knownHosts)
    {
        return new ConnectionOpenOkBody(knownHosts);
    }

    public final ConnectionRedirectBody createConnectionRedirectBody(final AMQShortString host,
                                                                         final AMQShortString knownHosts)
    {
        return new ConnectionRedirectBody(_protocolVersion,
                                              host,
                                              knownHosts);
    }

    public final ConnectionCloseBody createConnectionCloseBody(final int replyCode,
                                                                   final AMQShortString replyText,
                                                                   final int classId,
                                                                   final int methodId)
    {
        return new ConnectionCloseBody(_protocolVersion,
                                           replyCode,
                                           replyText,
                                           classId,
                                           methodId);
    }

    public final ConnectionCloseOkBody createConnectionCloseOkBody()
    {
        return ProtocolVersion.v0_8.equals(_protocolVersion)
                ? ConnectionCloseOkBody.CONNECTION_CLOSE_OK_0_8
                : ConnectionCloseOkBody.CONNECTION_CLOSE_OK_0_9;
    }


    public final ExchangeDeclareBody createExchangeDeclareBody(final int ticket,
                                                         final String exchange,
                                                         final String type,
                                                         final boolean passive,
                                                         final boolean durable,
                                                         final boolean autoDelete,
                                                         final boolean internal,
                                                         final boolean nowait,
                                                         final Map<String,Object> arguments)
    {

        return new ExchangeDeclareBody(ticket,
                                       AMQShortString.valueOf(exchange),
                                       AMQShortString.valueOf(type),
                                       passive,
                                       durable,
                                       autoDelete,
                                       internal,
                                       nowait,
                                       FieldTable.convertToFieldTable(arguments));
    }

    public final ExchangeDeclareOkBody createExchangeDeclareOkBody()
    {
        return new ExchangeDeclareOkBody();
    }

    public final ExchangeDeleteBody createExchangeDeleteBody(final int ticket,
                                                             final String exchange,
                                                             final boolean ifUnused,
                                                             final boolean nowait)
    {
        return new ExchangeDeleteBody(ticket,
                                      AMQShortString.valueOf(exchange),
                                      ifUnused,
                                      nowait
        );
    }

    public final ExchangeDeleteOkBody createExchangeDeleteOkBody()
    {
        return new ExchangeDeleteOkBody();
    }

    public final ExchangeBoundBody createExchangeBoundBody(final String exchange,
                                                           final String routingKey,
                                                           final String queue)
    {
        return new ExchangeBoundBody(AMQShortString.valueOf(exchange),
                                     AMQShortString.valueOf(routingKey),
                                     AMQShortString.valueOf(queue));
    }

    public final ExchangeBoundOkBody createExchangeBoundOkBody(final int replyCode,
                                                         final AMQShortString replyText)
    {
        return new ExchangeBoundOkBody(replyCode,
                                       replyText);
    }


    public final QueueDeclareBody createQueueDeclareBody(final int ticket,
                                                         final String queue,
                                                         final boolean passive,
                                                         final boolean durable,
                                                         final boolean exclusive,
                                                         final boolean autoDelete,
                                                         final boolean nowait,
                                                         final Map<String,Object> arguments)
    {

        return new QueueDeclareBody(ticket,
                                    AMQShortString.valueOf(queue),
                                    passive,
                                    durable,
                                    exclusive,
                                    autoDelete,
                                    nowait,
                                    FieldTable.convertToFieldTable(arguments));
    }

    public final QueueDeclareOkBody createQueueDeclareOkBody(final AMQShortString queue,
                                                       final long messageCount,
                                                       final long consumerCount)
    {
        return new QueueDeclareOkBody(queue,
                                      messageCount,
                                      consumerCount);
    }

    public final QueueBindBody createQueueBindBody(final int ticket,
                                             final String queue,
                                             final String exchange,
                                             final String routingKey,
                                             final boolean nowait,
                                             final Map<String,Object> arguments)
    {

        return new QueueBindBody(ticket,
                                 AMQShortString.valueOf(queue),
                                 AMQShortString.valueOf(exchange),
                                 AMQShortString.valueOf(routingKey),
                                 nowait,
                                 FieldTable.convertToFieldTable(arguments));

    }


    public final QueueBindOkBody createQueueBindOkBody()
    {
        return new QueueBindOkBody();
    }

    public final QueuePurgeBody createQueuePurgeBody(final int ticket,
                                               final AMQShortString queue,
                                               final boolean nowait)
    {
        return new QueuePurgeBody(ticket,
                                  queue,
                                  nowait);
    }

    public final QueuePurgeOkBody createQueuePurgeOkBody(final long messageCount)
    {
        return new QueuePurgeOkBody(messageCount);
    }

    public final QueueDeleteBody createQueueDeleteBody(final int ticket,
                                                 final String queue,
                                                 final boolean ifUnused,
                                                 final boolean ifEmpty,
                                                 final boolean nowait)
    {
        return new QueueDeleteBody(ticket,
                                   AMQShortString.valueOf(queue),
                                   ifUnused,
                                   ifEmpty,
                                   nowait);
    }

    public final QueueDeleteOkBody createQueueDeleteOkBody(final long messageCount)
    {
        return new QueueDeleteOkBody(messageCount);
    }

    public final QueueUnbindBody createQueueUnbindBody(final int ticket,
                                                           final AMQShortString queue,
                                                           final AMQShortString exchange,
                                                           final AMQShortString routingKey,
                                                           final FieldTable arguments)
    {
        return new QueueUnbindBody(ticket,
                                       queue,
                                       exchange,
                                       routingKey,
                                       arguments);
    }

    public final QueueUnbindOkBody createQueueUnbindOkBody()
    {
        return new QueueUnbindOkBody();
    }


    public final TxSelectBody createTxSelectBody()
    {
        return TxSelectBody.INSTANCE;
    }

    public final TxSelectOkBody createTxSelectOkBody()
    {
        return TxSelectOkBody.INSTANCE;
    }

    public final TxCommitBody createTxCommitBody()
    {
        return TxCommitBody.INSTANCE;
    }

    public final TxCommitOkBody createTxCommitOkBody()
    {
        return TxCommitOkBody.INSTANCE;
    }

    public final TxRollbackBody createTxRollbackBody()
    {
        return TxRollbackBody.INSTANCE;
    }

    public final TxRollbackOkBody createTxRollbackOkBody()
    {
        return TxRollbackOkBody.INSTANCE;
    }

    public ProtocolVersion getProtocolVersion()
    {
        return _protocolVersion;
    }


}
