package com.ephuvex.sw_sample.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ephuvex.sw_sample.R;
import com.ephuvex.sw_sample.models.StringProperties;
import com.ephuvex.sw_sample.models.TwoPropObject;
import com.ephuvex.sw_sample.services.NetService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import okhttp3.Request;
import okhttp3.RequestBody;

import static com.ephuvex.sw_sample.Consts.CRASH_PATH;
import static com.ephuvex.sw_sample.Consts.PROPERTIES_PATH;
import static com.ephuvex.sw_sample.Consts.SPLITTER_PATH;
import static com.ephuvex.sw_sample.Consts.SPLIT_PATH;
import static com.ephuvex.sw_sample.Consts.STATE_PATH;
import static com.ephuvex.sw_sample.Consts.STRINGS_PATH;
import static com.ephuvex.sw_sample.Consts.URL;

/**
 * This activity has an interface for getting and setting a string called "state", getting and
 * setting a character called "splitter", getting the properties of state (specifically, if state is
 * a palindrome and state reversed), and getting state split into words by splitter. Additionally,
 * the activity can retrieve a JSON object with two properties, and cause a crash in the backing
 * web service. All actions are done over REST, with state and splitter being properties stored in
 * the backing RESTful web service and all business logic being done by said service.
 */
public class MainActivity extends AppCompatActivity {

	/**
	 * An instance of NetService to make calls to the backing web service.
	 */
	private NetService netService = new NetService();

	// UI Components
	private EditText stateEditText;
	private Button getStateButton;
	private Button submitStateButton;
	private EditText splitterEditText;
	private Button getSplitterButton;
	private Button submitSplitterButton;
	private TextView originalText;
	private TextView splitStateText;
	private TextView fifthCharText;
	private TextView palindromeText;
	private TextView reverseText;
	private Button getTwoPropButton;
	private TextView propOneText;
	private TextView propTwoText;
	private Button crashButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize UI components
		stateEditText = findViewById(R.id.stateEditText);
		getStateButton = findViewById(R.id.getStateButton);
		submitStateButton = findViewById(R.id.stateSubmitButton);
		splitterEditText = findViewById(R.id.splitterEditText);
		getSplitterButton = findViewById(R.id.getSplitterButton);
		submitSplitterButton = findViewById(R.id.submitSplitterButton);
		originalText = findViewById(R.id.originalText);
		splitStateText = findViewById(R.id.splitStateText);
		fifthCharText = findViewById(R.id.fifthCharText);
		palindromeText = findViewById(R.id.palindromeText);
		reverseText = findViewById(R.id.reverseText);
		getTwoPropButton = findViewById(R.id.getTwoPropButton);
		propOneText = findViewById(R.id.propOneText);
		propTwoText = findViewById(R.id.propTwoText);
		crashButton = findViewById(R.id.crashButton);

		// Set up buttons
		setupGetStateButton();
		setupSubmitStateButton();
		setupGetSplitterButton();
		setupSubmitSplitterButton();
		setupGetTwoPropButton();
		setupCrashButton();
	}

	/**
	 * Initializes getStateButton to get state from the web service, and puts the fetched state in
	 * stateEditText
	 */
	private void setupGetStateButton() {
		final Request getStateRequest = new Request.Builder()
				.url(URL + STRINGS_PATH + STATE_PATH)
				.build();

		final View.OnClickListener getStateButtonListener = makeOnClickListener(
				() -> getStateRequest,
				stateEditText,
				null,
				"Failed to get state!",
				this::onSuccessfulStateUpdate
		);

		getStateButton.setOnClickListener(getStateButtonListener);
	}

	/**
	 * Initializes submitStateButton to submit the string in stateEditText as the new state
	 */
	private void setupSubmitStateButton() {
		final View.OnClickListener submitStateButtonListener = makeOnClickListener(
				() -> {
					// Wrap in quotes for transmission
					final String requestBody = '"' + stateEditText.getText().toString() + '"';

					return new Request.Builder()
							.url(URL + STRINGS_PATH + STATE_PATH)
							.method("PUT", RequestBody.create(requestBody.getBytes()))
							.header("Content-Type", "application/json")
							.build();
				},
				stateEditText,
				() -> {
					// Ensure that the new state is at least one character long
					final String state = stateEditText.getText().toString();
					return state.length() >= 1;
				},
				"Failed to set state!",
				this::onSuccessfulStateUpdate
		);

		submitStateButton.setOnClickListener(submitStateButtonListener);
	}

	/**
	 * Initializes getSplitterButton to retrieve splitter from the web service, and put the fetched
	 * value in splitterEditText
	 */
	private void setupGetSplitterButton() {
		final Request getSplitterRequest = new Request.Builder()
				.url(URL + STRINGS_PATH + STATE_PATH + SPLITTER_PATH)
				.build();

		final View.OnClickListener getSplitterButtonListener = makeOnClickListener(
				() -> getSplitterRequest,
				splitterEditText,
				null,
				"Failed to get splitter!",
				this::onSuccessfulSplitterUpdate
		);

		getSplitterButton.setOnClickListener(getSplitterButtonListener);
	}

	/**
	 * Initializes submitSplitterButton to submit the character in splitterEditText to the web service
	 */
	private void setupSubmitSplitterButton() {
		final View.OnClickListener submitSplitterButtonListener = makeOnClickListener(
				() -> new Request.Builder()
						.url(URL + STRINGS_PATH + STATE_PATH + SPLITTER_PATH)
						.method("PUT", RequestBody.create(
								// Wrap in quotes for transmission, and replace [SPACE] with an actual space.
								// See below for details.
								('"' + splitterEditText.getText().toString()
										.replace("[SPACE]", " ") + '"').getBytes()
						))
						.header("Content-Type", "application/json")
						.build(),
				splitterEditText,
				() -> {
					// Replace [SPACE] with a normal space. Required because in order to make it clear that
					// splitter was a space when fetched, splitterEditText takes on the value of [SPACE].
					final String splitter = splitterEditText.getText().toString()
							.replace("[SPACE]", " ");

					// Ensure splitter is now only one character long, and that character isn't a null byte
					return splitter.length() == 1 && splitter.charAt(0) != 0;
				},
				"Failed to set splitter!",
				this::onSuccessfulSplitterUpdate
		);

		submitSplitterButton.setOnClickListener(submitSplitterButtonListener);
	}

	/**
	 * Initializes getTwoPropButton to get the two-property object from the web service
	 */
	private void setupGetTwoPropButton() {
		final Request getTwoPropRequest = new Request.Builder()
				.url(URL + STRINGS_PATH + "/twoProp")
				.build();

		final View.OnClickListener getTwoPropButtonListener = makeOnClickListener(
				() -> getTwoPropRequest,
				null,
				null,
				"Failed to get two-property object!",
				this::onSuccessfulGetTwoProp
		);

		getTwoPropButton.setOnClickListener(getTwoPropButtonListener);
	}

	/**
	 * Initializes crashButton to send a request which will crash the web service
	 */
	private void setupCrashButton() {
		final Request crashRequest = new Request.Builder()
				.url(URL + CRASH_PATH)
				.build();

		final View.OnClickListener crashButtonListener = makeOnClickListener(
				() -> crashRequest,
				null,
				null,
				"Web service has crashed!",
				null
		);

		crashButton.setOnClickListener(crashButtonListener);
	}

	/**
	 * Returns a new View.OnClickListener for a button. Used to initialize the buttons more easily.
	 * Support for verification of inputs (and highlighting EditTexts containing bad inputs), simple
	 * error reporting and response consuming callbacks is provided.
	 *
	 * @param requestSupplier      A Supplier which returns a request. The request cannot be passed
	 *                             directly, as this function is only called once when the activity is
	 *                             started, and the contents of the request will change from that
	 *                             point. Cannot be null.
	 * @param editText             The EditText to be highlighted if the verification function fails.
	 *                             Can be null if no such EditText exists.
	 * @param verificationFunc     A function which consumes a string and returns a boolean. This
	 *                             function should return true if a value is valid, and false
	 *                             otherwise. Can be null if no verification is required.
	 * @param onFailureMessage     The message to be displayed to the user if the REST request fails.
	 *                             Cannot be null.
	 * @param responseBodyConsumer A Consumer which will accept the response body after a successful
	 *                             REST request. Can be null if nothing should be done after a
	 *                             successful request.
	 * @return A new View.OnClickListener for a specific button.
	 */
	private View.OnClickListener makeOnClickListener(
			@NotNull final Supplier<Request> requestSupplier,
			@Nullable final EditText editText,
			@Nullable final Supplier<Boolean> verificationFunc,
			@NotNull final String onFailureMessage,
			@Nullable final Consumer<String> responseBodyConsumer
	) {
		return event -> {
			// If an EditText was passed, reset its background tint
			if (editText != null) {
				ViewCompat.setBackgroundTintList(editText, ColorStateList.valueOf(Color.TRANSPARENT));
			}

			// If there was no verification function, or if the verification function returned true,
			if (verificationFunc == null || verificationFunc.get()) {
				// Send the request
				netService.sendRequest(requestSupplier.get(),
						responseBodyConsumer,
						() -> showFailureToast(onFailureMessage)
				);
			} else {
				// Else if there was a verification function and it failed,
				if (editText != null) {
					// Set the background tint of the offending EditText to red to signify a bad value
					ViewCompat.setBackgroundTintList(editText, ColorStateList.valueOf(
							Color.valueOf(255, 0, 0, 120).toArgb()
					));
				}
			}
		};
	}

	/**
	 * Called when state is successfully retrieved or updated
	 *
	 * @param newState The new value of state.
	 */
	private void onSuccessfulStateUpdate(final String newState) {
		MainActivity.this.runOnUiThread(() -> stateEditText.setText(newState));
		updateProperties();
	}

	/**
	 * Called when splitter is successfully retrieved or updated
	 *
	 * @param newSplitterResponseString The new value of splitter, but surrounded with quotes.
	 */
	private void onSuccessfulSplitterUpdate(final String newSplitterResponseString) {
		// Strip off quotes
		final char newSplitterChar = newSplitterResponseString.charAt(1);

		// If the character was just a space, replace it with [SPACE] to make it more clear which char
		// was returned
		final String newSplitterDisplayString = newSplitterChar == ' ' ?
				"[SPACE]" :
				Character.toString(newSplitterChar);

		MainActivity.this.runOnUiThread(() -> splitterEditText.setText(newSplitterDisplayString));
		updateProperties();
	}

	/**
	 * Called when the two-property object is successfully retrieved
	 *
	 * @param twoPropObjectString The serialized representation of the retrieved two-property object.
	 */
	private void onSuccessfulGetTwoProp(final String twoPropObjectString) {
		try {
			// Deserialize twoPropObjectString to a TwoPropObject
			final TwoPropObject twoPropObject = new ObjectMapper().readValue(
					twoPropObjectString,
					TwoPropObject.class
			);

			MainActivity.this.runOnUiThread(() -> {
				propOneText.setText(twoPropObject.getProp1());
				propTwoText.setText(String.format(Locale.ENGLISH, "%d", twoPropObject.getProp2()));
			});
		} catch (JsonProcessingException e) {
			Log.e("PARSE", e.toString());
		}
	}

	/**
	 * Called when the value of splitter or state are retrieved or updated. Fetches the properties of
	 * state and the value of state split by splitter.
	 */
	private void updateProperties() {
		final Request getSplitStateRequest = new Request.Builder()
				.url(URL + STRINGS_PATH + STATE_PATH + SPLIT_PATH)
				.build();

		// Get the state after it's been split by splitter
		netService.sendRequest(
				getSplitStateRequest,
				response -> MainActivity.this.runOnUiThread(() -> splitStateText.setText(response)),
				() -> showFailureToast("Failed to get split state!")
		);

		final Request getStatePropertiesRequest = new Request.Builder()
				.url(URL + STRINGS_PATH + STATE_PATH + PROPERTIES_PATH)
				.build();

		// Get the new properties of state
		netService.sendRequest(
				getStatePropertiesRequest,
				response -> {
					try {
						// Deserialize response into a StringProperties instance
						final StringProperties stringProperties = new ObjectMapper().readValue(
								response,
								StringProperties.class
						);

						MainActivity.this.runOnUiThread(() -> {
							originalText.setText(stringProperties.getOriginalString());
							fifthCharText.setText(stringProperties.getFifthChar());
							palindromeText.setText(Boolean.toString(stringProperties.getIsPalindrome()));
							reverseText.setText(stringProperties.getReversed());
						});
					} catch (JsonProcessingException e) {
						showFailureToast("Failed to get properties!");
						Log.e("PARSE", e.toString());
					}
				},
				() -> showFailureToast("Failed to get properties!")
		);
	}

	/**
	 * Shows a small text message at the bottom of the screen to provide feedback about any errors
	 *
	 * @param text The text to display in said message.
	 */
	private void showFailureToast(@NotNull final String text) {
		MainActivity.this.runOnUiThread(() -> Toast.makeText(
				MainActivity.this,
				text,
				Toast.LENGTH_SHORT
		).show());
	}
}
