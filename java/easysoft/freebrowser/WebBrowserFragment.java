package easysoft.freebrowser;


import android.Manifest;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.*;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

import static easysoft.freebrowser.FileBrowser.*;


public class WebBrowserFragment extends Fragment {
    Context context;
    View view;
    FrameLayout webLayout;
    WebView webView;
    RelativeLayout mainRel, review;
    LinearLayout steerLin, header;
    ImageView[] steerImgs;
    ImageView switcher;

    AnimationDrawable timerAnimation;
    ImageView timeImage;

    int webViewWidth, webViewHeight;

    String uri = "", runningUrl = "", actionId = "";
    ArrayList<String> urlCollection;
    int urlCollectionCounter;

    String orientation = "Portrait";

    boolean ishandled = false, actionIdChanged = false;
    private String sFileName, sUrl, sUserAgent;
    public WebBrowserFragment() {

    }

    public static WebBrowserFragment newInstance() {
        WebBrowserFragment fragment = new WebBrowserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        if (getArguments() != null) {
            uri = getArguments().getString("URL");
        }
        runningUrl = uri;
        urlCollection = new ArrayList<>();
        urlCollectionCounter = -1;
        webLayout = fileBrowser.frameLy.get(8);
        webViewWidth = displayWidth;
        webViewHeight = displayHeight;

        fileBrowser.changeIcon(headMenueIcon[6], "headMenueIcons", "closed", "running");
        fileBrowser.changeIcon(headMenueIcon[6], "headMenueIcons", "open", "running");


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }

        view = inflater.inflate(R.layout.fragment_web_browser, container, false);
        mainRel = (RelativeLayout) view.findViewById(R.id.webBrowserMainRel);
        //AnimationTimer
        timeImage = new ImageView(context);
        timeImage.setBackgroundResource(R.drawable.timer);
        timeImage.setLayoutParams(new FrameLayout.LayoutParams(displayWidth / 8, displayWidth / 8));
        timeImage.setX(displayWidth - displayWidth / 4);
        timeImage.setY(displayHeight / 8);
        timeImage.setVisibility(View.INVISIBLE);
        timeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeImage.setVisibility(View.INVISIBLE);
                timerAnimation.stop();
            }
        });

        timerAnimation = (AnimationDrawable) timeImage.getBackground();
        //
        mainRel.addView(WebAction(0));
        mainRel.addView(timeImage);

        mainRel.addView(createSteerIcons());
        mainRel.addView(createSwitcher());
        mainRel.addView(createReview());
        webLayout.bringToFront();
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1001){
            if (grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if (!sUrl.equals("")&&!sFileName.equals("")&&!sUserAgent.equals("")){
                    downloadFile(sFileName,sUrl,sUserAgent);
                }
            }
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void popupSoftkeyboard() {
        if (fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible()) {
            if (fileBrowser.showMessage == null || !fileBrowser.showMessage.isVisible())
                calledBack = "WebView";
            else
                calledBack = "";
            int fact = webViewHeight / 18,
                    fact01 = webViewHeight / 18;
            if (yfact < 0.625) {
                fact = webViewHeight / 28;
                fact01 = 0;
            }
            if (yfact >= 0.8) {
                fact01 = webViewHeight / 12;
            }
            fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6, "softKeyBoard", null, 5, (int) (2 * webViewHeight / 3 - fact),
                    displayWidth - 10, (int) (webViewHeight / 3 + fact01));
        }
    }

    private void popdownSoftkeyboard() {
        if (fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible())
            fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager)
                fileBrowser.getSystemService(
                        Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(webView.getWindowToken(), 0);

    }

    private RelativeLayout createReview() {
        RelativeLayout.LayoutParams reviewParam = new RelativeLayout.LayoutParams(webViewHeight / 22, webViewHeight / 22);
        reviewParam.addRule(RelativeLayout.CENTER_IN_PARENT);
        review = new RelativeLayout(fileBrowser);
        review.setLayoutParams(reviewParam);
        review.setX((float) (displayWidth - webViewHeight / 20));
        review.setY((float) (webViewHeight - webViewHeight / 14));

        ImageView reviewImg = new ImageView(fileBrowser);
        reviewImg.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/review_open.png"));
        reviewImg.setLayoutParams(reviewParam);
        reviewImg.setTag("review_closed.png");

        reviewImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = view.getTag().toString();
                if (tag.contains("closed")) {
                    view.setTag(view.getTag().toString().replace("closed", "open"));
                    steerLin.setY(webViewHeight);
                } else {
                    view.setTag(view.getTag().toString().replace("open", "closed"));
                    steerLin.setY(webViewHeight - webViewHeight / 10);
                }
                ((ImageView) view).setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/" + tag));

            }
        });
        review.addView(reviewImg);
        return review;
    }

    public void handleJavascriptInput(String charIndex, int startpos, int stoppos) {
        String script = "(function() {var element = document.getElementById('" + actionId + "')" +
                "; element.value = " + "'" + charIndex +
                "';element.setSelectionRange(" + startpos + ", " + stoppos + ");})();";

        fileBrowser.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(script, null);
            }
        });

        if (actionIdChanged) {
            Handler handler = new Handler(fileBrowser.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ie) {
                    }

                    hideKeyboard();
                    popupSoftkeyboard();
                }

            }, 25);
        }
        actionIdChanged = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public WebView WebAction(int angel) {

        webView = new createWebView();
        webView.loadUrl(runningUrl);
        return webView;
    }

    private void downloadFile(String filename, String url, String userAgent) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(url));

            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            DownloadManager dm = (DownloadManager) fileBrowser.getSystemService(fileBrowser.DOWNLOAD_SERVICE);
            dm.enqueue(request);

            Toast.makeText(fileBrowser, "Downloading File...", Toast.LENGTH_LONG).show();

            sUrl = "";
            sFileName = "";
            sUserAgent = "";
        }catch (Exception ignored){
            Toast.makeText(fileBrowser, ignored.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private ImageView createSwitcher() {

        switcher = new ImageView(fileBrowser);
        switcher.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth / 9, displayHeight / 2));
        switcher.setImageBitmap(fileBrowser.bitmapLoader("Icons/" + "switcher_closed.png"));
        switcher.setX(displayWidth - displayWidth / 9);
        switcher.setY(displayHeight / 13);
        switcher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ((ImageView) view).setImageBitmap(fileBrowser.bitmapLoader("Icons/" + "switcher_open.png"));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                }

                if (fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                    fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                }
                if (fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible()) {
                    fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
                }
                fileBrowser.startMovePanel(8);

                return true;
            }
        });
        return switcher;
    }

    private LinearLayout createSteerIcons() {
        int f = 18;
        if (yfact < 0.5)
            f = 20;

        steerLin = new LinearLayout(fileBrowser);
        steerLin.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams steerLinRel = new RelativeLayout.LayoutParams(5 * webViewWidth / 7, webViewHeight / 14);
        //steerLinRel.addRule(RelativeLayout.CENTER_HORIZONTAL);
        steerLin.setLayoutParams(steerLinRel);
        steerLin.setY(webViewHeight - webViewHeight / 10);
        steerLin.setX(0);

        //steerLin.setPadding(5,5,5,5);

        steerImgs = new ImageView[0];
        String[] steerNames = new String[]{"Rotation_closed.png", "Empty.png", "webSideback_closed.png", "webSideSearch.png", "https_closed.png", "webSideMemory_closed.png", "webSideforward_closed.png"};
        if (webViewHeight < displayHeight)
            steerNames = new String[]{"Rotation_closed.png"};

        for (int i = 0; i < steerNames.length; i++) {
            steerImgs = Arrays.copyOf(steerImgs, steerImgs.length + 1);
            steerImgs[steerImgs.length - 1] = new ImageView(fileBrowser);
            steerImgs[steerImgs.length - 1].setLayoutParams(new RelativeLayout.LayoutParams(webViewHeight / f,
                    webViewHeight / f));
            steerImgs[steerImgs.length - 1].setTag(steerNames[i]);
            if (orientation.equals("Landscape")) {
                steerImgs[steerImgs.length - 1].setLayoutParams(new RelativeLayout.LayoutParams(webViewWidth / f,
                        webViewWidth / f));
                steerImgs[steerImgs.length - 1].setTag(steerNames[i].replace("closed", "open"));
                steerLin.setLayoutParams(new RelativeLayout.LayoutParams(webViewWidth / f, webViewWidth / f));
                steerLin.setX(8 * displayHeight / 9);
            }
            steerImgs[steerImgs.length - 1].setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/" + steerImgs[steerImgs.length - 1].getTag().toString()));

            steerImgs[steerImgs.length - 1].setPadding(10, 0, 10, 0);
            if (urlCollection.size() == 0 && (i != 0 && i != 3 && i != 4 && i != 5))
                steerImgs[steerImgs.length - 1].setEnabled(false);


            if (!steerNames[i].startsWith("Empty"))
                steerImgs[steerImgs.length - 1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        steerPanelOnClick(view);
                    }
                });

            if (steerNames[i].contains("webSideMemory"))
                steerImgs[steerImgs.length - 1].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        steerPanelOnLongClick(v);

                        return true;
                    }
                });
            steerLin.addView(steerImgs[steerImgs.length - 1]);
        }
        return steerLin;
    }

    private void steerPanelOnClick(View v) {
        String tag = v.getTag().toString();

        if (tag.contains("webSideSearch")) {
            urlCollection = new ArrayList<>();
            urlCollectionCounter = -1;
            ishandled = false;
            fileBrowser.changeIcon(steerImgs[2], "browserIcons", "open", "closed");
            steerImgs[2].setEnabled(false);
            fileBrowser.changeIcon(steerImgs[6], "browserIcons", "open", "closed");
            steerImgs[4].setEnabled(false);

            webView.loadUrl(fileBrowser.searchMashineUrl);
        } else if (tag.contains("webSideMemory")) {
            if (fileBrowser.showList == null || !fileBrowser.showList.isVisible()) {
                String[] webSideMem = fileBrowser.read_writeFileOnInternalStorage("read", "WebSideMemory", "WebSideMemory_Saved.txt", "");
                if (webSideMem.length > 0) {

                    fileBrowser.changeIcon(steerImgs[5], "browserIcons", "closed", "open");

                    double f = 2.5;
                    if (yfact <= 0.625)
                        f = 2;

                    int[] iconpos = new int[2];
                    v.getLocationOnScreen(iconpos);

                    fileBrowser.createList("webSideMemoryList", 1, "WebSideMemory WebSideMemory_Saved.txt", 6,
                            (int) (iconpos[0]), (int) (iconpos[1] - webViewHeight / 22), (int) (webViewWidth / f), "lo");

                    fileBrowser.frameLy.get(3).bringToFront();

                }

            } else if (fileBrowser.showList != null || fileBrowser.showList.isVisible()) {
                fileBrowser.showList.showListLayout.removeView(fileBrowser.showList.trash);
                fileBrowser.changeIcon(steerImgs[5], "browserIcons", "open", "closed");
                fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
            }
        } else if (tag.contains("webSideback")) {
            ishandled = true;
            fileBrowser.changeIcon(steerImgs[6], "browserIcons", "closed", "open");
            steerImgs[6].setEnabled(true);
            urlCollectionCounter = urlCollectionCounter - 1;
            if (urlCollectionCounter == 0) {
                fileBrowser.changeIcon(steerImgs[2], "browserIcons", "open", "closed");
                steerImgs[2].setEnabled(false);
            }
            webView.loadUrl(urlCollection.get(urlCollectionCounter));
        } else if (tag.contains("webSideforward")) {
            ishandled = true;
            fileBrowser.changeIcon(steerImgs[2], "browserIcons", "closed", "open");
            steerImgs[2].setEnabled(true);
            urlCollectionCounter = urlCollectionCounter + 1;
            if (urlCollection.size() - 1 == urlCollectionCounter) {
                fileBrowser.changeIcon(steerImgs[6], "browserIcons", "open", "closed");
                ishandled = false;
                steerImgs[6].setEnabled(false);
            }
            webView.loadUrl(urlCollection.get(urlCollectionCounter));
        } else if (tag.contains("https")) {
            if (tag.contains("closed")) {
                fileBrowser.changeIcon(steerImgs[4], "browserIcons", "closed", "open");
                createHttpsEditField();
            } else if (tag.contains("open")) {
                if ((fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible()))
                    fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);
                fileBrowser.changeIcon(steerImgs[4], "browserIcons", "open", "closed");
                popdownSoftkeyboard();
            }
        } else if (tag.contains("Rotation")) {
            mainRel.removeView(header);
            mainRel.removeView(steerLin);
            mainRel.removeView(review);

            int angel;

            if (v.getTag().toString().contains("closed")) {
                orientation = "Landscape";
                webViewWidth = displayHeight;
                webViewHeight = displayWidth;
                v.setTag(v.getTag().toString().replace("closed", "open"));
                webLayout.setX(webViewHeight);

                angel = 90;
            } else {
                orientation = "Portrait";
                webViewWidth = displayWidth;
                webViewHeight = displayHeight;
                v.setTag(v.getTag().toString().replace("open", "closed"));
                webLayout.setX(0);
                webLayout.setY(0);
                angel = 0;
            }
            //
            RelativeLayout.LayoutParams webViewParams = new RelativeLayout.LayoutParams(webViewWidth, webViewHeight);
            webViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            webView.setLayoutParams(webViewParams);
            webLayout.setLayoutParams(new FrameLayout.LayoutParams(webViewWidth, webViewHeight));
            webLayout.setPivotX(0);
            webLayout.setPivotY(0);
            webLayout.setRotation(angel);

            ((ImageView) v).setImageBitmap(bitmapLoader("Icons/browserIcons/" + v.getTag().toString()));

            if (orientation.equals("Portrait")) {
                mainRel.addView(createSteerIcons());
                mainRel.addView(createSwitcher());
                mainRel.addView(createReview());
            } else if (orientation.equals("Landscape")) {
                mainRel.addView(createSteerIcons());
            }

        }

    }

    private void steerPanelOnLongClick(View v) {
        if (fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
            fileBrowser.changeIcon(steerImgs[3], "browserIcons", "open", "closed");
            fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
        } else {
            String[] webSideMem = fileBrowser.read_writeFileOnInternalStorage("read", "WebSideMemory", "WebSideMemory_Saved.txt", "");
            if (webSideMem.length == 0) {
                webSideMem = Arrays.copyOf(webSideMem, webSideMem.length + 1);
                webSideMem[webSideMem.length - 1] = "";
            }
            if (webSideMem.length > 20)
                webSideMem = Arrays.copyOfRange(webSideMem, 2, webSideMem.length);

            webSideMem = Arrays.copyOf(webSideMem, webSideMem.length + 1);
            webSideMem[webSideMem.length - 1] = webView.getUrl();
            String body = "";
            for (String s : webSideMem)
                body = body + s + "\n";

            if (fileBrowser.read_writeFileOnInternalStorage("write", "WebSideMemory", "WebSideMemory_Saved.txt", body.substring(0, body.lastIndexOf("\n"))).length == 0)
                fileBrowser.messageStarter("WebSideSaved", docu_Loader("Language/" + language + "/WebSideMemory_Save.txt"), 5000);
        }
    }

    private void createHttpsEditField() {
        Handler handler = new Handler();
        fileBrowser.messageStarter("httpsRequest", docu_Loader("Language/" + language + "/Instruction_Https.txt"),
                0);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible()) {
                    popupSoftkeyboard();
                }
            }
        }, 200);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            fileBrowser.fragmentShutdown(fileBrowser.webBrowserDisplay, 8);
        }
    }

    class createWebView extends WebView {

        public createWebView() {
            super(fileBrowser.context);
            this.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            initWebSetting();
            this.requestFocus();
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                        if (fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible())
                            fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
                    return false;
                }
            });


            this.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    String filename = URLUtil.guessFileName(url,contentDisposition,getMimeType(url));
                    sFileName = filename;
                    sUrl = url;
                    sUserAgent = userAgent;
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                        if (ContextCompat.checkSelfPermission(fileBrowser, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED){
                            downloadFile(filename,url,userAgent);
                        }else {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1001);
                        }
                    }else {
                        downloadFile(filename,url,userAgent);
                    }

                }
            });
            this.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    if (urlCollection.size() > 1) {
                        urlCollection.remove(urlCollectionCounter);
                        urlCollectionCounter = urlCollectionCounter - 1;
                    }
                    fileBrowser.changeIcon(steerImgs[2], "browserIcons", "open", "closed");
                    steerImgs[2].setEnabled(false);

                    fileBrowser.changeIcon(steerImgs[6], "browserIcons", "open", "closed");
                    steerImgs[6].setEnabled(false);

                    if (description.contains("ERR_UNKNOWN_URL_SCHEME")) {
                        webView.loadUrl(urlCollection.get(urlCollectionCounter));
                        fileBrowser.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fileBrowser.startExtApp(failingUrl);
                            }
                        });

                    } else
                        fileBrowser.messageStarter("notFoundWebPage", fileBrowser.docu_Loader("Language/" + fileBrowser.language + "/NoFoundWebSide.txt"), 6000);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    final String injectedJs = "javascript:(function(){" + fileBrowser.injectedJs("JS/getSelectedObject.js") + "})()";
                    view.loadUrl(injectedJs);
                    if (fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible())
                        fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);

                    runningUrl = url;
                    boolean contain = false;
                    if (urlCollectionCounter > -1 && (urlCollection.get(urlCollectionCounter)).startsWith(url.substring(0, url.length() - 10)))
                        contain = true;

                    if (!urlCollection.contains(url)) {
                        if (contain) {
                            urlCollection.remove(urlCollectionCounter);
                            urlCollectionCounter = urlCollectionCounter - 1;
                        }
                        if (ishandled && steerImgs.length > 1) {
                            for (int i = urlCollection.size() - 1; i > urlCollectionCounter; i--)
                                urlCollection.remove(i);
                            fileBrowser.changeIcon(steerImgs[5], "browserIcons", "open", "closed");
                            steerImgs[6].setEnabled(false);
                        }

                        urlCollection.add(webView.getUrl());
                        urlCollectionCounter = urlCollectionCounter + 1;
                        if (urlCollectionCounter > 0 && steerImgs.length > 1) {
                            fileBrowser.changeIcon(steerImgs[2], "browserIcons", "closed", "open");
                            steerImgs[2].setEnabled(true);
                            steerImgs[4].setEnabled(true);
                        }
                    }
                    timeImage.setVisibility(View.INVISIBLE);
                    timerAnimation.stop();
                }
            });

            this.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onPermissionRequest(PermissionRequest request) {
                    super.onPermissionRequest(request);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                }

            });

            this.addJavascriptInterface(
                    new Object() {
                        @JavascriptInterface
                        public void onClick(String tag, String id, String type) {

                            actionId = id;
                            actionIdChanged = true;
                            if ((type.contains("search") || type.contains("text")) && (tag.contains("INPUT") || tag.contains("TEXT"))) {
                                calledFrom = "";
                                if (id.endsWith("p") || id.endsWith("q") || id.endsWith("s") || id.contains("search")) {
                                    calledFrom = "SearchEngine_" + id;
                                } else
                                    calledFrom = "EditSearch";

                                handleJavascriptInput("", 0, 0);
                            }


                        }
                    },
                    "appHost"
            );

        }

        public void initWebSetting() {
            WebSettings settings = this.getSettings();
            settings.setJavaScriptEnabled(true);

            settings.setUseWideViewPort(true);
            settings.setPluginState(WebSettings.PluginState.ON);

            settings.setAllowFileAccess(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);

            settings.setDatabaseEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            settings.setLightTouchEnabled(true);
            settings.setBuiltInZoomControls(true);
            settings.setSupportZoom(true);
            settings.setDisplayZoomControls(false);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setPluginState(WebSettings.PluginState.ON);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                settings.setAllowFileAccessFromFileURLs(true);
            }
        }

    }
}