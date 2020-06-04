package com.example.wehelp.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.wehelp.R;
import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ServiceCall;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.DialogNodeOutputOptionsElement;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import com.ibm.watson.assistant.v2.model.SessionResponse;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;

import java.util.ArrayList;
import java.io.InputStream;
import java.util.List;

public class ChatBotMessage extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Chat_adapter mAdapter;
    private ArrayList messageArrayList;
    private EditText inputMessage;
    private ImageButton btnSend;
    private ImageButton btnRecord;
    StreamPlayer streamPlayer = new StreamPlayer();
    private boolean initialRequest;
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String TAG = "ChatBotMessage";
    private static final int RECORD_REQUEST_CODE = 101;
    private boolean listening = false;
    private MicrophoneInputStream capture;
    private Context mContext;
    private MicrophoneHelper microphoneHelper;

    private Assistant watsonAssistant;
    private Response<SessionResponse> watsonAssistantSession;
    private SpeechToText speechService;
    private TextToSpeech textToSpeech;

    private void createServices() {
        watsonAssistant = new Assistant("2020-05-28", new IamAuthenticator(mContext.getString(R.string.assistant_apikey)));
        watsonAssistant.setServiceUrl(mContext.getString(R.string.assistant_url));

        textToSpeech = new TextToSpeech(new IamAuthenticator((mContext.getString(R.string.TTS_apikey))));
        textToSpeech.setServiceUrl(mContext.getString(R.string.TTS_url));

        speechService = new SpeechToText(new IamAuthenticator(mContext.getString(R.string.STT_apikey)));
        speechService.setServiceUrl(mContext.getString(R.string.STT_url));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot_message);

        mContext = getApplicationContext();

        inputMessage = findViewById(R.id.message);
        btnSend = findViewById(R.id.btn_send);
        btnRecord = findViewById(R.id.btn_record);
//        String customFont = "Montserrat-Regular.ttf";
//        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
//        inputMessage.setTypeface(typeface);
        recyclerView = findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();
        mAdapter = new Chat_adapter(messageArrayList);
        createServices();
        microphoneHelper = new MicrophoneHelper(ChatBotMessage.this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        this.inputMessage.setText("");
        this.initialRequest = true;

        //checking if the user has granted the permission to record audio or not
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "User has denied to record audio");
            makeRequest();
        } else {
            Log.i(TAG, "User has already granted permission to record audio");
        }


inputMessage.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if(inputMessage.getText().length()<=0)
        {
            btnSend.setEnabled(false);
        }
        else
        {
            btnSend.setEnabled(true);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(inputMessage.getText().length()<=0)
        {
            btnSend.setEnabled(false);
        }
        else
        {
            btnSend.setEnabled(true);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(inputMessage.getText().length()<=0)
        {
            btnSend.setEnabled(false);
        }
        else
        {
            btnSend.setEnabled(true);
        }
    }
});
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection()) {
                    sendMessage();
                }
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordMessage();
            }
        });
        sendMessage();

        //to turn the text in to speech when the user clicks on the message
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                BotMessage audioMessage = (BotMessage) messageArrayList.get(position);
                if (audioMessage != null && !audioMessage.getMessage().isEmpty()) {
                    new SayTask().execute(audioMessage.getMessage());
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                recordMessage();

            }
        }));

    };

    // Speech-to-Text Record Audio permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;

            case RECORD_REQUEST_CODE: {
                    if (grantResults.length == 0
                            || grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "User has already granted permission to record audio");
                    } else {
                        Log.i(TAG, "User has denied to record audio");
                    }
                    return;
                }

                case MicrophoneHelper.REQUEST_PERMISSION: {
                    if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(this, "User has denied to record audio", Toast.LENGTH_SHORT).show();
                    }
            }
        }

    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MicrophoneHelper.REQUEST_PERMISSION);
    }


    // Sending a message to Watson Assistant Service
    private void sendMessage() {

        final String inputmessage = this.inputMessage.getText().toString().trim();

            if (!this.initialRequest) {
                BotMessage inputMessage = new BotMessage();
                inputMessage.setMessage(inputmessage);
                inputMessage.setId("1");
                messageArrayList.add(inputMessage);
            } else {
                BotMessage inputMessage = new BotMessage();
                inputMessage.setMessage(inputmessage);
                inputMessage.setId("100");
                this.initialRequest = false;
                Toast.makeText(getApplicationContext(), "Tap on the message for Voice", Toast.LENGTH_LONG).show();

            }

            this.inputMessage.setText("");
            mAdapter.notifyDataSetChanged();

            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        if (watsonAssistantSession == null) {
                            ServiceCall<SessionResponse> call = watsonAssistant.createSession(new CreateSessionOptions.Builder().assistantId(mContext.getString(R.string.assistant_id)).build());
                            watsonAssistantSession = call.execute();
                        }

                            MessageInput input = new MessageInput.Builder()
                                    .text(inputmessage)
                                    .build();
                            MessageOptions options = new MessageOptions.Builder()
                                    .assistantId(mContext.getString(R.string.assistant_id))
                                    .input(input)
                                    .sessionId(watsonAssistantSession.getResult().getSessionId())
                                    .build();
                            Response<MessageResponse> response = watsonAssistant.message(options).execute();
                            Log.i(TAG, "run: " + response.getResult());
                            if (response != null &&
                                    response.getResult().getOutput() != null &&
                                    !response.getResult().getOutput().getGeneric().isEmpty()) {

                                List<RuntimeResponseGeneric> responses = response.getResult().getOutput().getGeneric();

                                for (RuntimeResponseGeneric r : responses) {
                                    BotMessage outMessage;
                                    switch (r.responseType()) {
                                        case "text":
                                            outMessage = new BotMessage();
                                            outMessage.setMessage(r.text());
                                            outMessage.setId("2");

                                            messageArrayList.add(outMessage);
                                            break;

                                        case "option":
                                            outMessage = new BotMessage();
                                            String title = r.title();
                                            String OptionsOutput = "";
                                            for (int i = 0; i < r.options().size(); i++) {
                                                DialogNodeOutputOptionsElement option = r.options().get(i);
                                                OptionsOutput = OptionsOutput + option.getLabel() + "\n";

                                            }
                                            outMessage.setMessage(title + "\n" + OptionsOutput);
                                            outMessage.setId("2");

                                            messageArrayList.add(outMessage);

                                            break;


                                        default:
                                            Log.e("Error", "Unhandled message type");
                                    }
                                }

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                        if (mAdapter.getItemCount() > 1) {
                                            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);

                                        }

                                    }
                                });
                            }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();

    }


    //Record a message via Watson Speech to Text
    private void recordMessage() {
        if(ContextCompat.checkSelfPermission(ChatBotMessage.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {
            makeRequest();
        }
        else {
            if (listening != true) {
                capture = microphoneHelper.getInputStream(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            speechService.recognizeUsingWebSocket(getRecognizeOptions(capture), new MicrophoneRecognizeDelegate());
                        } catch (Exception e) {
                            showError(e);
                        }
                    }
                }).start();
                listening = true;
                Toast.makeText(ChatBotMessage.this, "Listening....Click to Stop", Toast.LENGTH_LONG).show();

            } else {
                try {
                    microphoneHelper.closeInputStream();
                    listening = false;
                    Toast.makeText(ChatBotMessage.this, "Stopped Listening....Click to Start", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    /**
     * Check Internet Connection
     *
     * @return
     */
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Check for network connections
        if (isConnected) {
            return true;
        } else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    //Private Methods - Speech to Text
    private RecognizeOptions getRecognizeOptions(InputStream audio) {
        return new RecognizeOptions.Builder()
                .audio(audio)
                .contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .build();
    }

    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputMessage.setText(text);
            }
        });
    }

    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnRecord.setEnabled(true);
            }
        });
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatBotMessage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }


    private class SayTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            streamPlayer.playStream(textToSpeech.synthesize(new SynthesizeOptions.Builder()
                    .text(params[0])
                    .voice(SynthesizeOptions.Voice.EN_US_LISAVOICE)
                    .accept(HttpMediaType.AUDIO_WAV)
                    .build()).execute().getResult());
            return "Did synthesize";
        }
    }

    //Watson Speech to Text Methods.
    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {
        @Override
        public void onTranscription(SpeechRecognitionResults speechResults) {
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                showMicText(text);
            }
        }

        @Override
        public void onError(Exception e) {
            showError(e);
            enableMicButton();
        }

        @Override
        public void onDisconnected() {
            enableMicButton();
        }

    }

    //class  created to turn the message into speech when the message is clicked
    private class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}

