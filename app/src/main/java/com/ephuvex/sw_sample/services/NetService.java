package com.ephuvex.sw_sample.services;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A service class for network calls. Supports the sending of REST requests, and handles receiving
 * responses to said requests.
 */
public class NetService {
	/**
	 * The OkHTTP client to use for sending REST requests.
	 */
	private final OkHttpClient okHttpClient = new OkHttpClient();

	/**
	 * Sends the provided request with okHttpClient. If the request succeeds, resultConsumer is
	 * called with the response body. If the request fails, onFailureRunnable is called.
	 *
	 * @param request The request to send.
	 * @param resultConsumer A consumer of the response body if the request succeeds.
	 * @param onFailureRunnable A runnable to call if the request fails.
	 */
	public void sendRequest(
			@NotNull final Request request,
			@Nullable final Consumer<String> resultConsumer,
			@NotNull final Runnable onFailureRunnable
	) {
		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				Log.e("NET", e.toString());
				onFailureRunnable.run();
			}

			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
				if (response.isSuccessful()) {
					if (resultConsumer != null) {
						//noinspection ConstantConditions -- response.body().string() can't be null, we checked
						final String responseBody = response.body() != null ? response.body().string() : "";
						resultConsumer.accept(responseBody);
					}
				} else {
					onFailureRunnable.run();
				}
			}
		});
	}
}
