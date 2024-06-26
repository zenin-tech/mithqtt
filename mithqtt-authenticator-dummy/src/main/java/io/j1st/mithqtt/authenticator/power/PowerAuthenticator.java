package io.j1st.mithqtt.authenticator.power;

import com.github.longkerdandy.mithqtt.api.auth.Authenticator;
import com.github.longkerdandy.mithqtt.api.auth.AuthorizeResult;
import io.j1st.power.storage.mongo.MongoStorage;
import io.j1st.power.storage.mongo.entity.AgentStatus;
import io.j1st.power.storage.mongo.entity.ProductStatus;
import io.j1st.power.storage.mongo.entity.ServiceType;
import io.netty.handler.codec.mqtt.MqttGrantedQoS;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import org.apache.commons.configuration.AbstractConfiguration;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Dummy Authenticator
 * This authenticator basically authorize everything, it should only been used for test purpose
 */
@SuppressWarnings("unused")
public class PowerAuthenticator implements Authenticator {

    Logger logger = LoggerFactory.getLogger(PowerAuthenticator.class);

    private boolean allowDollar;    // allow $ in topic
    private String deniedTopic;     // topic will be rejected
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
//        if (!mongoStorage.isAgentExists(clientId)) {
//            return AuthorizeResult.FORBIDDEN;
//        }
        //验证用户名密码是否合法及状态是否正常
        Integer status = mongoStorage.getPowerAgentAuth(userName, password);
        if (status == null || status == AgentStatus.DISABLED.value()) {
            return AuthorizeResult.FORBIDDEN;
        }
        //验证product状态是否正常
//        Integer status = this.mongoStorage.getProductStatusByAgentId(clientId);
//        if (status == null || !status.equals(ProductStatus.SERVICE.value())) {
//            return AuthorizeResult.FORBIDDEN;
//        }
       /* //验证所在的service是否链接已满
        String userId = this.mongoStorage.getOperatorIdByAgent(clientId);
        if (userId != null) {
            //link count
            long count = this.mongoStorage.getServiceCountByOperatorId(ServiceType.HARDWARE_MANAGER.value(), new ObjectId(userId));
            if (count <= 0) {
                return AuthorizeResult.FORBIDDEN;
            }
        }*/

        return AuthorizeResult.OK;
    }

    @Override
    public AuthorizeResult authPublish(String clientId, String userName, String topicName, int qos, boolean retain) {
        if (!this.allowDollar && topicName.startsWith("$")) return AuthorizeResult.FORBIDDEN;
        if (topicName.equals(this.deniedTopic)) return AuthorizeResult.FORBIDDEN;
        //判断topic是否包括自己的clientId
//        if(topicName.indexOf(clientId) == -1){
//            return AuthorizeResult.FORBIDDEN;
//        }
//        if(!topicName.endsWith("upstream")){
//            return AuthorizeResult.FORBIDDEN;
//        }
//        //验证product状态是否正常
//        Integer status = this.mongoStorage.getProductStatusByAgentId(clientId);
//        if (status == null || !status.equals(ProductStatus.SERVICE.value())) {
//            return AuthorizeResult.FORBIDDEN;
//        }
        // Validate Agent Connect Privilege
        if (this.mongoStorage.isDisableAgent(clientId, AgentStatus.DISABLED.value())) {
            return AuthorizeResult.FORBIDDEN;
        }
        return AuthorizeResult.OK;
    }

    @Override
    public List<MqttGrantedQoS> authSubscribe(String clientId, String userName, List<MqttTopicSubscription> requestSubscriptions) {
        List<MqttGrantedQoS> r = new ArrayList<>();
        requestSubscriptions.forEach(subscription -> {
            if (!this.allowDollar && subscription.topic().startsWith("$")) r.add(MqttGrantedQoS.FAILURE);
            if (subscription.topic().equals(this.deniedTopic)) r.add(MqttGrantedQoS.FAILURE);
            if (!subscription.topic().endsWith("downstream")) r.add(MqttGrantedQoS.FAILURE);
            if (!subscription.topic().contains(clientId)) r.add(MqttGrantedQoS.FAILURE);
            r.add(MqttGrantedQoS.valueOf(subscription.requestedQos().value()));
        });
        return r;
    }

    @Override
    public String oauth(String credentials) {
        return mongoStorage.getUserByToken(credentials);
    }
}
