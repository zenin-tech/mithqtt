package io.j1st.photovoltaic.authenticator;

import com.github.longkerdandy.mithqtt.api.auth.Authenticator;
import com.github.longkerdandy.mithqtt.api.auth.AuthorizeResult;
import io.j1st.power.storage.mongo.MongoStorage;
import io.j1st.power.storage.mongo.entity.AgentStatus;
import io.j1st.smartcharger.authenticator.SmartChargerAuthenticator;
import io.netty.handler.codec.mqtt.MqttGrantedQoS;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PhotoVoltaicAuthenticator implements Authenticator {


    Logger logger = LoggerFactory.getLogger(PhotoVoltaicAuthenticator.class);

    /**
     * allow $ in topic
     */
    private boolean allowDollar;

    /**
     * topic will be rejected
     */
    private String deniedTopic;

    /**
     * database
     */
    protected MongoStorage mongoStorage;

    @Override
    public void init(AbstractConfiguration config) {
        this.allowDollar = config.getBoolean("allowDollar", true);
        this.deniedTopic = config.getString("deniedTopic", null);
        mongoStorage = new MongoStorage();
        mongoStorage.init(config);

    }

    @Override
    public void destroy() {

    }

    @Override
    public AuthorizeResult authConnect(String clientId, String userName, String password) {
        //验证clentId是否有效
//        if (!mongoStorage.isPvAgentExists(clientId)) {
//            return AuthorizeResult.FORBIDDEN;
//        }
//        //验证用户名密码是否合法
//        if (!mongoStorage.isPvAgentAuth(userName, password)) {
//            return AuthorizeResult.FORBIDDEN;
//        }
//
//        // Validate Agent Connect Privilege
//        if (this.mongoStorage.isPvDisableAgent(clientId, AgentStatus.DISABLED.value())) {
//            return AuthorizeResult.FORBIDDEN;
//        }
        //查询网关的状态
        Integer status = this.mongoStorage.getPvGatewayStatus(userName,password);
        if(status == null || status == AgentStatus.DISABLED.value()){
            return AuthorizeResult.FORBIDDEN;
        }
        return AuthorizeResult.OK;
    }

    @Override
    public AuthorizeResult authPublish(String clientId, String userName, String topicName, int qos, boolean retain) {
        if (!this.allowDollar && topicName.startsWith("$")) {
            return AuthorizeResult.FORBIDDEN;
        }
        if (topicName.equals(this.deniedTopic)) {
            return AuthorizeResult.FORBIDDEN;
        }
        // Validate Agent Connect Privilege
//        if (this.mongoStorage.isPvDisableAgent(clientId, AgentStatus.DISABLED.value())) {
//            return AuthorizeResult.FORBIDDEN;
//        }

        return AuthorizeResult.OK;
    }

    @Override
    public List<MqttGrantedQoS> authSubscribe(String clientId, String userName, List<MqttTopicSubscription> requestSubscriptions) {
        List<MqttGrantedQoS> r = new ArrayList<>();
        // subscription
        requestSubscriptions.forEach(subscription -> {
            if (!this.allowDollar && subscription.topic().startsWith("$")) {
                r.add(MqttGrantedQoS.FAILURE);
            }
            if (subscription.topic().equals(this.deniedTopic)) {
                r.add(MqttGrantedQoS.FAILURE);
            }
            r.add(MqttGrantedQoS.valueOf(subscription.requestedQos().value()));
        });

        return r;
    }

    @Override
    public String oauth(String credentials) {
        return mongoStorage.getUserByToken(credentials);
    }
}
