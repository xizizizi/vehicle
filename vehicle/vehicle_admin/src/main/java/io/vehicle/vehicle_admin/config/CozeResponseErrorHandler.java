package io.vehicle.vehicle_admin.config;

import io.vehicle.vehicle_admin.exception.CozeApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
public class CozeResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String responseBody = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        log.error("Coze API 请求失败 - 状态码: {}, 响应: {}",
                response.getStatusCode(), responseBody);

        throw new CozeApiException(
                "Coze API 请求失败: " + response.getStatusCode() + " - " + responseBody
        );
    }
}