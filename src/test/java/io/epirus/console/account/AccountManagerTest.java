/*
 * Copyright 2020 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.epirus.console.account;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import io.epirus.console.config.CliConfig;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountManagerTest {
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;
    static OkHttpClient mockedOkHttpClient = mock(OkHttpClient.class);
    static Call call = mock(Call.class);
    static ConnectionPool connectionPool = mock(ConnectionPool.class);
    static AccountManager accountManager;

    @BeforeAll
    public static void setUpStreams() throws IOException {
        accountManager =
                new AccountManager(
                        CliConfig.getConfig(
                                new File(String.valueOf(CliConfig.getDefaultEpirusConfigPath()))),
                        mockedOkHttpClient);
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterAll
    public static void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testAccountConfirmation() throws IOException, InterruptedException {
        Request request =
                accountManager.createRequest(accountManager.createRequestBody("test@gmail.com"));
        Response response =
                new Response.Builder()
                        .protocol(Protocol.H2_PRIOR_KNOWLEDGE)
                        .message("")
                        .body(
                                ResponseBody.create(
                                        "{\"active\":true}", MediaType.parse("application/json")))
                        .code(200)
                        .request(request)
                        .build();
        when(call.execute()).thenReturn(response);
        when(mockedOkHttpClient.newCall(any(Request.class))).thenReturn(call);
        when(mockedOkHttpClient.connectionPool()).thenReturn(connectionPool);
        doNothing().when(connectionPool).evictAll();
        accountManager.checkIfAccountIsConfirmed();
        Assertions.assertTrue(outContent.toString().contains("Account is active."));
    }

    @Test
    public void testAccountCreation() throws IOException {
        Request request =
                accountManager.createRequest(accountManager.createRequestBody("test@gmail.com"));
        Response response =
                new Response.Builder()
                        .protocol(Protocol.H2_PRIOR_KNOWLEDGE)
                        .message("")
                        .body(
                                ResponseBody.create(
                                        "{\n"
                                                + "    \"token\": \"8190c700-1f10-4c50-8bb2-1ce78bf0412b\",\n"
                                                + "    \"createdTimestamp\": \"1583234909601\"\n"
                                                + "}",
                                        MediaType.parse("application/json")))
                        .code(200)
                        .request(request)
                        .build();
        when(call.execute()).thenReturn(response);
        when(mockedOkHttpClient.newCall(any(Request.class))).thenReturn(call);
        when(mockedOkHttpClient.connectionPool()).thenReturn(connectionPool);
        doNothing().when(connectionPool).evictAll();
        accountManager.createAccount("test@gmail.com");
        Assertions.assertTrue(outContent.toString().contains("Account created successfully."));
    }
}
