package com.github.longkerdandy.mithqtt.http.resources;

import com.github.longkerdandy.mithqtt.api.auth.Authenticator;
import com.github.longkerdandy.mithqtt.api.comm.HttpCommunicator;
import com.github.longkerdandy.mithqtt.api.internal.Disconnect;
import com.github.longkerdandy.mithqtt.api.internal.InternalMessage;
import com.github.longkerdandy.mithqtt.api.metrics.MetricsService;
import com.github.longkerdandy.mithqtt.http.entity.ResultEntity;
import com.github.longkerdandy.mithqtt.http.util.Validator;
import com.github.longkerdandy.mithqtt.storage.redis.sync.RedisSyncStorage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;

/**
 * MQTT disconnect related resource
 */
@Path("/clients/{clientId}/disconnect")
@Produces(MediaType.APPLICATION_JSON)
public class MqttDisconnectResource extends AbstractResource {

    private static final Logger logger = LoggerFactory.getLogger(MqttDisconnectResource.class);

    public MqttDisconnectResource(String serverId, Validator validator, RedisSyncStorage redis, HttpCommunicator communicator, Authenticator authenticator, MetricsService metrics) {
        super(serverId, validator, redis, communicator, authenticator, metrics);
    }

    /**
     * 断开连接
     * @param clientId
     * @return
     * @throws UnsupportedEncodingException
     */
    @GET
    public ResultEntity<Boolean> disconnect(@PathParam("clientId") String clientId) {

       logger.info("clientId {} disconnect to rabbitmq server",clientId);

        String internalIp = this.authenticator.getClientInternalIp(clientId);
        if (StringUtils.isNotBlank(internalIp)) {
            InternalMessage<String> internalMessage = new InternalMessage<>();
            internalMessage.setUserName(clientId);
            internalMessage.setClientId(clientId);
            internalMessage.setMessageType(MqttMessageType.DISCONNECT);
            this.communicator.sendToBrokerInternal(internalIp, internalMessage);
            return new ResultEntity<>(true);
        }

        String brokerId = this.redis.getConnectedNode(clientId);
       if(StringUtils.isBlank(brokerId)){
           return new ResultEntity<>(null);
       }
        InternalMessage<Disconnect> dis = new InternalMessage<>();
        dis.setUserName(clientId);
        dis.setClientId(clientId);
        dis.setMessageType(MqttMessageType.DISCONNECT);
        try {
            //断开设备
            this.communicator.sendToBroker(brokerId, dis);
            //通知应用模块
            this.communicator.sendToApplication(dis);
            logger.info("clientId {} disconnect to rabbitmq server success ",clientId);
            return new ResultEntity<>(true);
        }catch (Exception e) {
            return new ResultEntity<>(false);
        }
    }


}
