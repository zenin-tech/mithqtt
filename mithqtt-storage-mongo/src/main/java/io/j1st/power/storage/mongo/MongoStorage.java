package io.j1st.power.storage.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import io.j1st.power.storage.mongo.entity.Permission;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;


/**
 * Created by Administrator on 2016/4/27.
 */
public class MongoStorage {

    protected MongoClient client;
    protected MongoDatabase database;
    protected MongoDatabase brokerData;

    public void init(AbstractConfiguration config) {
        // MongoClient
//        List<ServerAddress> addresses = parseAddresses(config.getString("mongo.address"));
//        List<MongoCredential> credentials = parseCredentials(
//                config.getString("mongo.userName"),
//                "admin",
//                config.getString("mongo.password"));
//        if (addresses.size() == 1) {
//            this.client = new MongoClient(addresses.get(0), credentials);
//        } else {
//            this.client = new MongoClient(addresses, credentials);
//        }
        MongoClientURI uri = new MongoClientURI(config.getString("mongo.url"));
        this.client = new MongoClient(uri);
        this.database = this.client.getDatabase(config.getString("mongo.database"));

        String broker = config.getString("mongo.broker");
        if (StringUtils.isNotBlank(broker)) {
            this.brokerData = this.client.getDatabase(config.getString("mongo.broker"));
        }
    }


    public void destroy() {
        if (this.client != null) this.client.close();
    }

    private ServerAddress parseAddress(String address) {
        int idx = address.indexOf(':');
        return (idx == -1) ?
                new ServerAddress(address) :
                new ServerAddress(address.substring(0, idx), Integer.parseInt(address.substring(idx + 1)));
    }

    private List<ServerAddress> parseAddresses(String addresses) {
        List<ServerAddress> result = new ArrayList<>();
        String[] addrs = addresses.split(" *, *");
        for (String addr : addrs) {
            result.add(parseAddress(addr));
        }
        return result;
    }

    private List<MongoCredential> parseCredentials(String userName, String database, String password) {
        List<MongoCredential> result = new ArrayList<>();
        result.add(MongoCredential.createCredential(userName, database, password.toCharArray()));
        return result;
    }


    /* =========================================== CPS Agent Operations ===============================================*/


    /**
     * 设备合法性验证
     *
     * @param userName 采集器Id
     * @return 采集器 or Null
     */
    public Integer getPowerAgentAuth(String userName, String password) {
        Document doc = this.database.getCollection("agents")
                .find(and(eq("_id", new ObjectId(userName)), eq("token", password))).first();
        if(doc != null && doc.get("status") != null) {
            return doc.getInteger("status");
        }
        return null;
    }


 /* =========================================== 充电桩项目验证方法 ===============================================*/


    /**
     * 判断设备是否存在
     *
     * @param number 设备编号
     * @return 采集器 or Null
     */
    public boolean isAgentExists(String number) {
        return this.database.getCollection("gateway_information")
                .find(eq("gateway_number", number)).first() != null;
    }


    /**
     * 设备合法性验证
     *
     * @param userName 采集器Id
     * @return 采集器 or Null
     */
    public boolean isAgentAuth(String userName, String password) {
        return this.database.getCollection("gateway_information")
                .find(and(eq("gateway_number", userName), eq("token", password))).first() != null;
    }


    /**
     * 获取 用户信息，根据 Token
     *
     * @param token Token
     * @return 用户信息 or Null
     */
    public String getUserByToken(String token) {
        Document d = this.database.getCollection("users")
                .find(eq("token", token))
                .projection(exclude("password"))
                .first();
        if (d == null) {
            return null;
        }
        return d.getObjectId("_id").toString();
    }

    /**
     * 判断agent是否可连接
     *
     * @param number 设备编号
     * @param status  设备状态
     * @return is exist
     */
    public boolean isDisableAgent(String number, int status) {
        return this.database.getCollection("gateway_information")
                .find(and(eq("gateway_number", number), eq("status", status)))
                .first() != null;

    }

    /*===================================================单相机项目验证合法性逻辑==========================================*/

    /**
     * 判断设备是否存在
     *
     * @param number 设备编号
     * @return 采集器 or Null
     */
    public boolean isPvAgentExists(String number) {
        return this.database.getCollection("gateways")
                .find(eq("gateway_id", number)).first() != null;
    }


    /**
     * 设备合法性验证
     *
     * @param userName 采集器Id
     * @return 采集器 or Null
     */
    public boolean isPvAgentAuth(String userName, String password) {
        return this.database.getCollection("gateways")
                .find(and(eq("gateway_id", userName), eq("token", password))).first() != null;
    }


    /**
     * 判断agent是否可连接
     *
     * @param number 设备编号
     * @param status 设备状态
     * @return is exist
     */
    public boolean isPvDisableAgent(String number, int status) {
        return this.database.getCollection("gateways")
                .find(and(eq("gateway_id", number), eq("status", status)))
                .first() != null;

    }

    /**
     * 设备合法性验证
     *
     * @param userName 采集器Id
     * @return 采集器 or Null
     */
    public Integer getPvGatewayStatus(String userName, String password) {
        Document doc = this.database.getCollection("gateways")
                .find(and(eq("gateway_id", userName), eq("token", password))).first();
        if(doc != null && doc.get("status") != null){
            return doc.getInteger("status");
        }
        return null;
    }

    public String getClientInternalIp(String clientId){
        Document doc = this.brokerData.getCollection("mqtt_client")
                .find(and(eq("client_id", clientId))).first();
        if (doc == null) {
            return null;
        }
        return doc.getString("internal_ip");
    }
}
