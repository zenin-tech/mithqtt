package com.github.longkerdandy.mithqtt.http.resources;

import com.github.longkerdandy.mithqtt.api.auth.AuthorizeResult;
import com.github.longkerdandy.mithqtt.api.internal.InternalMessage;
import com.github.longkerdandy.mithqtt.api.internal.Publish;
import com.github.longkerdandy.mithqtt.api.metrics.MetricsService;
import com.github.longkerdandy.mithqtt.http.entity.ErrorCode;
import com.github.longkerdandy.mithqtt.http.entity.ErrorEntity;
import com.github.longkerdandy.mithqtt.http.entity.ResultEntity;
import com.github.longkerdandy.mithqtt.http.exception.AuthorizeException;
import com.github.longkerdandy.mithqtt.http.exception.ValidateException;
import com.github.longkerdandy.mithqtt.http.util.Validator;
import com.github.longkerdandy.mithqtt.storage.redis.sync.RedisSyncStorage;
import com.github.longkerdandy.mithqtt.util.Topics;
import com.github.longkerdandy.mithqtt.api.auth.Authenticator;
import com.github.longkerdandy.mithqtt.api.comm.HttpCommunicator;
import com.sun.security.auth.UserPrincipal;
import io.dropwizard.auth.Auth;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MQTT Publish related resource
 */
@Path("/clients/{clientId}/publish")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.TEXT_PLAIN)
public class MqttPublishResource extends AbstractResource {

    private static final Logger logger = LoggerFactory.getLogger(MqttPublishResource.class);

    public MqttPublishResource(String serverId, Validator validator, RedisSyncStorage redis, HttpCommunicator communicator, Authenticator authenticator, MetricsService metrics) {
        super(serverId, validator, redis, communicator, authenticator, metrics);
    }

    /**
     * 下发字符串
     * @param clientId
     * @param user
     * @param protocol
     * @param dup
     * @param qos
     * @param topicName
     * @param packetId
     * @param body
     * @param translate
     * @return
     * @throws UnsupportedEncodingException
     */
    //@PermitAll
    @POST
    public ResultEntity<Boolean> publish(@PathParam("clientId") String clientId,
                                         @Auth UserPrincipal user,
                                         @QueryParam("protocol") @DefaultValue("4") byte protocol,
                                         @QueryParam("dup") @DefaultValue("false") boolean dup,
                                         @QueryParam("qos") @DefaultValue("0") int qos,
                                         @QueryParam("topicName") String topicName,
                                         @QueryParam("packetId") @DefaultValue("0") int packetId,
                                         String body,
                                         @QueryParam("isTranslate") @DefaultValue("false") boolean translate) throws UnsupportedEncodingException {

       logger.info("clientId {} publish message to rabbitmq ,topic = {}",clientId,topicName);

        byte[] payload;
        if (translate) {
            // TODO change to HexString
            payload = hexStr2Bytes(body);
        } else {
            payload = body == null ? null : body.getBytes("ISO-8859-1");
        }
        //下发到消息队列中
        boolean isPublish = publishMessage(clientId,protocol,dup,qos,topicName,packetId,payload);
        return new ResultEntity<>(isPublish);
    }


    /**
     * 下发字节数组
     * @param clientId
     * @param user
     * @param protocol
     * @param dup
     * @param qos
     * @param topicName
     * @param packetId
     * @param payload
     * @param translate
     * @return
     * @throws UnsupportedEncodingException
     */
    @Path("/bytes")
    @POST
    public ResultEntity<Boolean> publish(@PathParam("clientId") String clientId,
                                         @Auth UserPrincipal user,
                                         @QueryParam("protocol") @DefaultValue("4") byte protocol,
                                         @QueryParam("dup") @DefaultValue("false") boolean dup,
                                         @QueryParam("qos") @DefaultValue("0") int qos,
                                         @QueryParam("topicName") String topicName,
                                         @QueryParam("packetId") @DefaultValue("0") int packetId,
                                         byte[] payload,
                                         @QueryParam("isTranslate") @DefaultValue("false") boolean translate) throws UnsupportedEncodingException {

        logger.info("clientId {} publish message to rabbitmq ,topic = {}",clientId,topicName);

        //下发到消息队列中
        boolean isPublish = publishMessage(clientId,protocol,dup,qos,topicName,packetId,payload);
        return new ResultEntity<>(isPublish);
    }


    /**
     * 元用户权限下发字符串
     *
     * @param clientId  agent id
     * @param protocol  mqtt protocol
     * @param dup       dup
     * @param qos       qos
     * @param topicName topicName
     * @param packetId  packetId
     * @param body      boby
     * @param translate translate
     * @return result
     * @throws UnsupportedEncodingException
     */
    @Path("/hardware")
    @POST
    public ResultEntity<Boolean> publishToHardware(@PathParam("clientId") String clientId,
                                                   @QueryParam("protocol") @DefaultValue("4") byte protocol,
                                                   @QueryParam("dup") @DefaultValue("false") boolean dup,
                                                   @QueryParam("qos") @DefaultValue("0") int qos,
                                                   @QueryParam("topicName") String topicName,
                                                   @QueryParam("packetId") @DefaultValue("0") int packetId,
                                                   String body,
                                                   @QueryParam("isTranslate") @DefaultValue("false") boolean translate) throws UnsupportedEncodingException {

        logger.info("clientId {} publish message to rabbitmq ,topic = {}", clientId, topicName);
        byte[] payload;
        if (translate) {
            // TODO change to HexString
            payload = hexStr2Bytes(body);
        } else {
            payload = body == null ? null : body.getBytes("ISO-8859-1");
        }
        boolean isPublish = publishMessage(clientId,protocol,dup,qos,topicName,packetId,payload);
        return new ResultEntity<>(isPublish);
    }



    /**
     * 元用户权限下发字节数组
     *
     * @param clientId  agent id
     * @param protocol  mqtt protocol
     * @param dup       dup
     * @param qos       qos
     * @param topicName topicName
     * @param packetId  packetId
     * @param payload      payload
     * @param translate translate
     * @return result
     * @throws UnsupportedEncodingException
     */
    @Path("/hardware/bytes")
    @POST
    public ResultEntity<Boolean> publishBytesToHardware(@PathParam("clientId") String clientId,
                                                   @QueryParam("protocol") @DefaultValue("4") byte protocol,
                                                   @QueryParam("dup") @DefaultValue("false") boolean dup,
                                                   @QueryParam("qos") @DefaultValue("0") int qos,
                                                   @QueryParam("topicName") String topicName,
                                                   @QueryParam("packetId") @DefaultValue("0") int packetId,
                                                   byte[] payload,
                                                   @QueryParam("isTranslate") @DefaultValue("false") boolean translate) throws UnsupportedEncodingException {

        logger.info("clientId {} publish message to rabbitmq ,topic = {}", clientId, topicName);

        boolean isPublish = publishMessage(clientId,protocol,dup,qos,topicName,packetId,payload);
        return new ResultEntity<>(isPublish);
    }


    /**
     * bytes字符串转换为Byte值
     *
     * @param src String Byte字符串，每个Byte之间没有分隔符(字符范围:0-9 A-F)
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        /*对输入值进行规范化整理*/
        src = src.trim().replace(" ", "").toUpperCase(Locale.US);
        //处理值初始化
        int m = 0, n = 0;
        int iLen = src.length() / 2; //计算长度
        byte[] ret = new byte[iLen]; //分配存储空间

        for (int i = 0; i < iLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = (byte) (Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, n)) & 0xFF);
        }
        return ret;
    }


    /**
     * 下发消息
     * @param clientId
     * @param protocol
     * @param dup
     * @param qos
     * @param topicName
     * @param packetId
     * @param payload
     * @return
     */
    public Boolean publishMessage(String clientId,byte protocol,boolean dup,int qos,String topicName, int packetId,
                                  byte[] payload){

        String internalIp = this.authenticator.getClientInternalIp(clientId);
        if (StringUtils.isNotBlank(internalIp)) {
            logger.info(clientId+" sendToBrokerInternal");
            InternalMessage<Publish> internalMessage = new InternalMessage<>();
            internalMessage.setUserName(clientId);
            internalMessage.setClientId(clientId);
            internalMessage.setMessageType(MqttMessageType.PUBLISH);
            internalMessage.setQos(MqttQoS.valueOf(qos));
            Publish publish = new Publish(topicName, packetId, payload, System.currentTimeMillis());
            internalMessage.setPayload(publish);
            this.communicator.sendToBrokerInternal(internalIp, internalMessage);
            return true;
        }

        String userName = clientId;
        MqttVersion version = MqttVersion.fromProtocolLevel(protocol);

        // The Topic Name in the PUBLISH Packet MUST NOT contain wildcard characters
        // Validate Topic Name based on configuration
        if (!this.validator.isTopicNameValid(topicName)) {
            logger.debug("Protocol violation: Client {} sent PUBLISH message contains invalid topic name {}", clientId, topicName);
            throw new ValidateException(new ErrorEntity(ErrorCode.INVALID));
        }

        List<String> topicLevels = Topics.sanitizeTopicName(topicName);

        logger.debug("Message received: Received PUBLISH message from client {} topic {}", clientId, topicName);

        AuthorizeResult result = this.authenticator.authPublish(clientId, userName, topicName, qos, false);
        // Authorize successful
        if (result == AuthorizeResult.OK) {
            logger.trace("Authorization succeed: Publish to topic {} authorized for client {}", topicName, clientId);

            // Construct Internal Message
            Publish publish = new Publish(topicName, packetId, payload,System.currentTimeMillis());
            InternalMessage<Publish> msg = new InternalMessage<>(MqttMessageType.PUBLISH, dup, MqttQoS.valueOf(qos), false, version, clientId, userName, this.serverId, publish);

            // When sending a PUBLISH Packet to a Client the Server MUST set the RETAIN flag to 1 if a message is
            // sent as a result of a new subscription being made by a Client. It MUST set the RETAIN
            // flag to 0 when a PUBLISH Packet is sent to a Client because it matches an established subscription
            // regardless of how the flag was set in the message it received.

            // The Server uses a PUBLISH Packet to send an Application Message to each Client which has a
            // matching subscription.
            // When Clients make subscriptions with Topic Filters that include wildcards, it is possible for a Client’s
            // subscriptions to overlap so that a published message might match multiple filters. In this case the Server
            // MUST deliver the message to the Client respecting the maximum QoS of all the matching subscriptions.
            // In addition, the Server MAY deliver further copies of the message, one for each
            // additional matching subscription and respecting the subscription’s QoS in each case.
            Map<String, MqttQoS> subscriptions = new HashMap<>();
            this.redis.getMatchSubscriptions(topicLevels, subscriptions);
            subscriptions.forEach((cid, q) -> {

                // Compare publish QoS and subscription QoS
                MqttQoS fQos = qos > q.value() ? q : MqttQoS.valueOf(qos);

                // Each time a Client sends a new packet of one of these
                // types it MUST assign it a currently unused Packet Identifier. If a Client re-sends a
                // particular Control Packet, then it MUST use the same Packet Identifier in subsequent re-sends of that
                // packet. The Packet Identifier becomes available for reuse after the Client has processed the
                // corresponding acknowledgement packet. In the case of a QoS 1 PUBLISH this is the corresponding
                // PUBACK; in the case of QoS 2 it is PUBCOMP. For SUBSCRIBE or UNSUBSCRIBE it is the
                // corresponding SUBACK or UNSUBACK. The same conditions apply to a Server when it
                // sends a PUBLISH with QoS > 0
                // A PUBLISH Packet MUST NOT contain a Packet Identifier if its QoS value is set to
                int pid = 0;
                if (fQos == MqttQoS.AT_LEAST_ONCE || fQos == MqttQoS.EXACTLY_ONCE) {
                    pid = this.redis.getNextPacketId(cid);
                }

                // Construct Internal Message
                Publish p = new Publish(topicName, pid, payload,System.currentTimeMillis());
                InternalMessage<Publish> m = new InternalMessage<>(MqttMessageType.PUBLISH, false, fQos, false, MqttVersion.MQTT_3_1_1, cid, null, null, p);

                // Forward to recipient
                boolean d = false;
                String bid = this.redis.getConnectedNode(cid);
                if (StringUtils.isNotBlank(bid)) {
                    logger.trace("Communicator sending: Send PUBLISH message to broker {} for client {} subscription", bid, cid);
                    d = true;
                    this.communicator.sendToBroker(bid, m);
                    logger.info("clientId {} publish message to broker success . message topic = {}", clientId, topicName);
                }

                // In the QoS 1 delivery protocol, the Sender
                // MUST treat the PUBLISH Packet as “unacknowledged” until it has received the corresponding
                // PUBACK packet from the receiver.
                // In the QoS 2 delivery protocol, the Sender
                // MUST treat the PUBLISH packet as “unacknowledged” until it has received the corresponding
                // PUBREC packet from the receiver.
                if (fQos == MqttQoS.AT_LEAST_ONCE || fQos == MqttQoS.EXACTLY_ONCE) {
                    logger.trace("Add in-flight: Add in-flight PUBLISH message {} with QoS {} for client {}", pid, fQos, cid);
                    this.redis.addInFlightMessage(cid, pid, m, d);
                }
            });

            // Pass message to 3rd party application
            this.communicator.sendToApplication(msg);

            logger.info("clientId {} publish message to rabbitmq success . message topic = {}", clientId, topicName);
            return true;
        } else {
            logger.info("clientId {} publish message to rabbitmq  Authorize fail publish out . message topic = {}", clientId, topicName);
            throw new AuthorizeException(new ErrorEntity(ErrorCode.UNAUTHORIZED));
        }
    }

}
