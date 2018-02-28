package com.skynyrd.kafka.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skynyrd.kafka.Constants;
import com.skynyrd.kafka.Record;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public class ElasticClientImpl implements ElasticClient {
    private JestClient client;
    private static Logger log = LogManager.getLogger(ElasticClientImpl.class);

    public ElasticClientImpl(String url, int port) throws UnknownHostException {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(String.format("http://%s:%s", url, port))
                .multiThreaded(true)
                .build());

        client = factory.getObject();
    }


    public void bulkSend(List<Record> records, String index, String type) {
        Bulk.Builder bulkBuilder = new Bulk.Builder()
                .defaultIndex(index)
                .defaultType(type);

        for (Record record : records) {
            String id = record.getDataObject().get("id").getAsString();
            JsonObject data = record.getDataObject();

            bulkBuilder.addAction(new Index.Builder(data).id(id).build());
        }

        try {
            if (records.size() > 0) {
                BulkResult execute = client.execute(bulkBuilder.build());
                String errorMessage = execute.getErrorMessage();

                if (errorMessage != null) {
                    log.error(errorMessage);
                }
            }
        } catch (IOException e) {
            log.error(e.toString());
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        client.shutdownClient();
    }
}
