package easysoft.freebrowser;

import android.app.*;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;
import androidx.print.PrintHelper;

import java.io.*;
import java.lang.Process;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.*;
import static android.os.Build.VERSION.SDK_INT;


public class FileBrowser extends Activity  {
    final int ASK_PERMISSION_EXTStorage = 8080,
            ASK_PERMISSION_PKGInstall = 8081;

    Vibrator vibrator = null;
    Context context;

    AnimationDrawable timerAnimation;
    ImageView timeImage;

    FrameLayout protector;
    static ImageView[] timerPNGs = new ImageView[7];
    ActivityManager aM;
    FragmentManager fragManager;
    FragmentTransaction fragTrans;
    ArrayList<FrameLayout> frameLy;
    SoftKeyBoard softKeyBoard;
    showMessageFragment showMessage;
    showListFragment showList;
    fileBrowser_01Fragment filebrowser_01;
    MediaDisplayFragment showMediaDisplay;
    TextEditorFragment createTxEditor;
    DisplayMetrics displayMetrics;
    emailDisplayFragment createSendEmail;
    WebBrowserFragment webBrowserDisplay;
    RelativeLayout mainRelDisplay;
    Intent progrIntent;

    GridLayout mainDisplayGrid;
    HorizontalScrollView headMScroll;
    RelativeLayout folderPanelFrame;
    RelativeLayout gridMiddleFrame;

    public Uri fileUri;
    public String filePath;
    static String openFrags = "";
    static FileBrowser fileBrowser;
    static AssetManager asma;
    static PackageManager pgMa;
    static ScrollView scrollView;
    static ImageView[] headMenueIcon;
    static ImageView[] headMenueIcon01;
    static ImageView[] headMenueIcon02;
    static ImageView[] headMenueIcon03;
    static ImageView HidedView;
    static EditText keyboardTrans;

    static int  displayHeight, displayWidth, hide;
    static int panel_direction = 1, pdfPageCount = 0;
    static double xfact, yfact;
    static String device, devicePath="", devicePath_trans;
    static String language = "";
    static String commandString = "";
    static String urldevice, memoryUrlDevice;
    static String searchMashineUrl;
    static String calledBy = "", calledFrom = "",calledBack ="";
    static ArrayList<String[]> arrayList;
    static ArrayList<String[]> paramList;
    static ArrayList<String[]> memoryParamList;
    static ArrayList<String> selectedFile;
    static ArrayList<String> transList;
    static ArrayList<String> runningMediaList;
    static String memoryDevicePath = "";
    static Integer[] detscrPosY;
    static Integer[] memoryDetscrPosY = new Integer[0];
    static int textSize = 0;
    static int selectedTx = -1;
    static int selectedTx02 = -1;
    static int basescrPosY = 0;
    static int memoryBasescrPosY = 0;
    static int mainscrPosX = 0;
    static int memoryMainscrPosX = 0;
    static int memorySelectedTx;
    static int[] firstRun = new int[0];
    static boolean timerRun = false, canWrite = false, insertaction = false, intendStarted = false, threadStop = false, isPdf = false;

    int fragId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_browser);
        fileBrowser = this;
        context = this;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mainRelDisplay = findViewById(R.id.mainRel);
        mainRelDisplay.setBackgroundColor(getResources().getColor(R.color.grey));

        asma = getAssets();
        aM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        pgMa = getPackageManager();
        fragManager = this.getFragmentManager();
        fragTrans = null;

        language = Locale.getDefault().getDisplayLanguage();
        if (!language.equals("Deutsch"))
            language = "English";

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        if (displayWidth >= displayHeight) {
            xfact = (double) displayWidth / displayHeight;
            yfact = (double) displayHeight / displayWidth;
        } else {
            xfact = (double) displayHeight / displayWidth;
            yfact = (double) displayWidth / displayHeight;
        }

        if (yfact < 0.62)
            textSize = 10;
        else
            textSize = 14;

        if(yfact <= 0.6 && displayWidth < 500) {
            xfact = 8*xfact/9;
            textSize = 9;
        }
        else if(yfact <= 0.5 && displayWidth > 1000) {
            textSize = 12;
            xfact = 2*xfact/3;
        }
        else if(yfact < 0.6 && displayWidth > 1000) {
            textSize = 12;
        }

        frameLy = new ArrayList<>();
        for(int i=0;i<10;i++) {
            frameLy.add(null);
        }
        hide = 1;
        device = "/storage";
        searchMashineUrl = "https://de.search.yahoo.com/";

        //AnimationTimer
        timeImage = new ImageView(this);
        timeImage.setBackgroundResource(R.drawable.timer);
        timeImage.setLayoutParams(new FrameLayout.LayoutParams(displayWidth / 8, displayWidth / 8));
        timeImage.setX(displayWidth - displayWidth / 4);
        timeImage.setY(displayHeight / 8);
        timeImage.setVisibility(View.INVISIBLE);
        mainRelDisplay.addView(timeImage);

        timerAnimation = (AnimationDrawable) timeImage.getBackground();
        //

        createSurface();

        if(!Api30and()) {
            if (checkPermission()) {
                // if permission is already granted display a toast message
                Log.e("Permission Granted..", "jetzt");
            } else {
                if (protector == null) {
                    protector = new FrameLayout(this);
                    protector.setEnabled(false);
                    protector.setLayoutParams(new FrameLayout.LayoutParams(displayWidth, displayHeight));
                    protector.setBackgroundColor(getResources().getColor(R.color.blue_overlay));
                    protector.setClickable(true);
                    mainRelDisplay.addView(protector);
                }
                requestPermission();
            }
        }

        String from = getIntent().getStringExtra("FROM"),
                url = getIntent().getStringExtra("URL");

        if(from != null) {
            startExtApp(url);
        }
    }


    public boolean checkPermission() {
        int vibrate_permission = ContextCompat.checkSelfPermission(getApplicationContext(), VIBRATE);
        int read_permission = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int write_permission = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int installpackage_permission = ContextCompat.checkSelfPermission(getApplicationContext(), INSTALL_PACKAGES);

        return vibrate_permission == PackageManager.PERMISSION_GRANTED
                && read_permission == PackageManager.PERMISSION_GRANTED && write_permission == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission() {
        int PERMISSION_REQUEST_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{VIBRATE, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean vibrateaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean readaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            boolean writeaccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
            //boolean installpackageaccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
            if (vibrateaccepted && readaccepted && writeaccepted) {
                mainRelDisplay.removeView(protector);

                fileBrowser.messageStarter("Instruction_Manuel", docu_Loader("Language/" + language + "/Instruction_Manuel.txt"),
                        0);
            } else {
                fileBrowser.messageStarter("PermissionDenied", docu_Loader("Language/" + language + "/Canceled_Permission.txt"),
                        5000);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getWindow().setSoftInputMode(WindowManager.
                LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[7], "sideRightMenueIcons", "open", "closed");

    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (ASK_PERMISSION_PKGInstall): {

                fileBrowser.messageStarter("Instruction_Manuel", docu_Loader("Language/" + language + "/Instruction_Manuel.txt"),
                        0);

                break;
            }
            case (4010): {
                fileBrowser.createList_systemUrl(2,4);
                fileBrowser.changeIcon(headMenueIcon01[1],"sideLeftMenueIcons","closed","open");
                break;
            }
        }
    }

    void serveAPK(String url, String mimeType){

        String PATH = url;
        File file = new File(PATH);
        if(file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uriFromFile(getApplicationContext(), new File(PATH)), mimeType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                getApplicationContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in opening the file!");
            }
        }else{
            Toast.makeText(getApplicationContext(),"installing",Toast.LENGTH_LONG).show();
        }
    }
    Uri uriFromFile(Context context, File file) {

        if (SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public boolean Api30and() {
        if (SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    //Uri uri = Uri.parse("package:" + BuildConfig.LIBRARY_PACKAGE_NAME);
                    Uri uri = Uri.parse("package:" + context.getApplicationContext().getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                    startActivityForResult(intent, ASK_PERMISSION_PKGInstall);
                } catch (Exception ex) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, ASK_PERMISSION_PKGInstall);
                }

            }
            return true;
        }
        return false;
    }
    ///
    public void isExternalStorageWritable(String filePath) {
        boolean externalStorageWritable = isExternalStorageWritable();
        File file = new File(filePath);
        boolean canWrite = file.canWrite();
        boolean isFile = file.isFile();
        boolean isDirectory = file.isDirectory();
        long usableSpace = file.getUsableSpace();

        Log.d("externalStorageWritable: ",  "" + externalStorageWritable);
        Log.d("filePath: ", "" + filePath);
        Log.d("canWrite: ", "" + canWrite);
        Log.d("isFile: ", "" + isFile);
        Log.d("isDirectory: ", "" + isDirectory);
        Log.d("usableSpace: ", "" + usableSpace);
    }
    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    ///
    public boolean haveNetwork(){
        boolean have_WIFI= false;
        boolean have_MobileData = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for(NetworkInfo info:networkInfos){
            if (info.getTypeName().equalsIgnoreCase("WIFI"))if (info.isConnected())have_WIFI=true;
            if (info.getTypeName().equalsIgnoreCase("MOBILE"))if (info.isConnected())have_MobileData=true;
        }
        return have_WIFI||have_MobileData;
    }


    /* returns external storage paths */
    public String[] getExternalStorageDirectories() {

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = getExternalFilesDirs(null);
            String internalRoot = Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase();

            for (File file : externalDirs) {
                if(file==null) //solved NPE on some Lollipop devices
                    continue;
                String path = file.getPath().split("/Android")[0];

                if(path.toLowerCase().startsWith(internalRoot))
                    continue;

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                return new String[]{"no found"};
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d("LOG_TAG", results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    Log.d("LOG_TAG", results.get(i)+" might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }

    public ArrayList createArrayList(String urldevicePath) {
        ArrayList<String[]> arrList = new ArrayList<>();
        selectedFile = new ArrayList<>();
        //
        String[] s = new File(urldevice).list(),
                sb = new String[0];

        if (s != null)
            for (String sc : s)
                if (hide == 1 && !sc.startsWith(".")) {
                    sb = Arrays.copyOf(sb, sb.length + 1);
                    sb[sb.length - 1] = sc;
                }
        if (hide == 1)
            s = sb;
        arrList.add(s);
        ///
        if(!urldevice.equals(urldevicePath)) {
            String[] lineElements = urldevicePath.split("/");
            String st = "";
            int n = 0;
            for (String s1 : lineElements) {
                selectedFile.add(s1);
            }

            while (n < selectedFile.size()) {
                sb = new String[0];
                st = st + "/" + selectedFile.get(n);
                s = new File(urldevice + st).list();
                if (s != null)
                    for (String sc : s)
                        if (hide == 1 && !sc.startsWith(".")) {
                            sb = Arrays.copyOf(sb, sb.length + 1);
                            sb[sb.length - 1] = sc;
                        }
                if (hide == 1)
                    s = sb;
                arrList.add(s);
                n++;
            }
        }
        ////
        return arrList;
    }

    public void createList_systemUrl(int column, int maxsize) {
        float w = 4;
        if(yfact < 0.625)
            w = 3;
        int width = (int)(displayWidth/w);

        int[] iconpos = new int[2];
        headMenueIcon01[1].getLocationOnScreen(iconpos);
        iconpos[0] = iconpos[0];

        arrayList = new ArrayList<>(0);

        final String[] dev = new String[]{"Intern","SD-Card","USB"};
        String[] devices = new String[] {"/storage/self/primary"};
        for(String s: getExternalStorageDirectories()) {
            devices = Arrays.copyOf(devices,devices.length +1);
            devices[devices.length -1] = s;
        }
        for(int count=0;count<devices.length;count++)
            arrayList.add(new String[]{dev[count], devices[count]});


        Bundle bund = new Bundle();
        bund.putInt("COLUMN_COUNT", column);
        bund.putString("CALLER", "startListUrl");

        float mS = (float)(calculateListLength(arrayList.size(), 10));
        if(mS < displayHeight/20)
            mS = displayHeight/20;

        iconpos[1] = iconpos[1] -((int)mS + headMenueIcon01[1].getHeight()/2) ;
        if(iconpos[1] <=0) {
            iconpos[1] = displayHeight/22;
            mS = mS/2;
        }


        fragmentStart(showList, 3,"list_url", bund, iconpos[0], iconpos[1], width, (int) mS);
    }

    public void createList(String kind, int column, String fUrl, int maxsize, int x, int y, int width, String direction) {
        float mS = 0;
        String[][] listString = new String[0][0];
        String[] servedList = new String[0];
        if(fUrl.contains("/")) {
            servedList = docu_Loader(fUrl);
        } else if(fUrl.contains(" ")){
            String folder = fUrl.substring(0,fUrl.indexOf(" ")),
                    file = fUrl.substring(fUrl.indexOf(" ")+1);
            servedList = fileBrowser.read_writeFileOnInternalStorage("read", folder, file, "");
        }
        if(!fUrl.equals("")) {
            arrayList = new ArrayList<>(0);

            if (kind.equals("mailSaveList"))
                servedList = Arrays.copyOfRange(servedList, 2, servedList.length);

            for (int i = 0; i < servedList.length; i++) {
                listString = Arrays.copyOf(listString, listString.length + 1);
                listString[listString.length - 1] = new String[1];
                listString[listString.length - 1][0] = servedList[i];
            }
            for (int i = 0; i < listString.length; i++)
                arrayList.add(listString[i]);
        }

        mS = calculateListLength(arrayList.size(), 6);
        if(mS > maxsize*6*textSize)
            mS = maxsize*6*textSize;

        Bundle bund = new Bundle();
        bund.putInt("COLUMN_COUNT", column);
        bund.putString("CALLER", kind);


        if(kind.equals("mailList")) {
            double flfact = 1.5;
            if(yfact > 0.625)
                flfact = 1.70;
            mS = (float) (flfact * mS);
        } else if(kind.equals("TrashList")) {
            double flfact = 2;
            if(yfact > 0.625)
                flfact = 2.2;
            mS = (float) (flfact * mS);
        }

        switch(direction) {
            case("lo"): {
                y = (int)(y -mS) -10;
                x = x - width;
                break;
            }
            case("ro"): {
                y = (int)(y -mS);
                break;
            }
            case("lu"): {
                x = x -width;
                break;
            }
            case("ru"): {
                break;
            }
        }
        if(width/mS < 0.3)
            mS = mS/2;

        fragmentStart(showList, 3,"list", bund, x, y, width, (int) mS);
    }

    public float calculateListLength (int listLength, float multipl) {
        float mS = 0;
        mS = listLength * (multipl*textSize);

        return mS;
    }

    static public Bitmap bitmapLoader(String url) {
        InputStream inputStream;
        BufferedInputStream buffIn;
        Bitmap bmp = null;

        try {
            if(calledBy.equals("mediaPlayer") || calledBy.equals("textEditor") || calledBy.equals("importImg")) {
                inputStream = new FileInputStream(new File(url));
                calledBy = "";
            }
            else
                inputStream = asma.open(url);
            buffIn = new BufferedInputStream(inputStream);
            bmp = BitmapFactory.decodeStream(buffIn);

            inputStream.close();
            buffIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bmp;
    }

    public String[] file_icon_Loader(String url) {
        String[] list = new String[0];
        String[] listcorr = new String[0];
        try {
            list = getAssets().list(url);
        } catch (IOException ie) {
        }

        for (int i = 0; i < list.length; i++) {
            if (list[i].contains("closed") || list[i].contains("Empty")) {
                listcorr = Arrays.copyOf(listcorr, listcorr.length + 1);
                listcorr[listcorr.length - 1] = list[i];

            }
        }
        return listcorr;
    }

    public static String[] docu_Loader(String paramString) {
        int i = 0;
        String[] arrayOfString = new String[0];
        InputStream inputStream = null;
        try {
            if(calledBy.equals("extFileCall") || calledBy.equals("scriptMail")) {
                inputStream = new FileInputStream(paramString);
                calledBy = "";
            } else
                inputStream = asma.open(paramString);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String strRead;
            while ((strRead = bufferedReader.readLine()) != null) {
                arrayOfString = Arrays.copyOf(arrayOfString, arrayOfString.length + 1);
                arrayOfString[arrayOfString.length - 1] = strRead;
            }
            inputStream.close();
            inputStreamReader.close();
            bufferedReader.close();
        } catch (Exception e) {
        }
        return arrayOfString;
    }

    public String injectedJs(String filePath) {
        BufferedReader stream = null;
        StringBuilder jsBuilder = new StringBuilder();
        try {
            stream = new BufferedReader(new InputStreamReader(getAssets().open(filePath)));
            String line;
            while ((line = stream.readLine()) != null) {
                jsBuilder.append(line.trim());
            }
            return jsBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    public String[] read_writeFileOnInternalStorage(String rw, String folder, String sFileName, String sBody) {
        String[] arrayOfString = new String[0];
        File dir;
        boolean append = false;
        if(calledBy.equals("trashList") || calledBy.equals("scriptMail")) {
            append = true;
            calledBy = "";
        }
        if(folder.contains("/storage"))
            dir = new File(folder);
        else
            dir = new File(context.getFilesDir(), folder);

        if (!dir.exists()) {
            dir.mkdir();
        }

        if (rw.equals("write") && !sBody.equals("delete"))
            try {

                File gpxfile = new File(dir, sFileName);
                gpxfile.setExecutable(true);
                FileWriter writer = new FileWriter(gpxfile,append);
                writer.append(sBody);
                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        else if (rw.equals("write") && sBody.equals("delete")) {
            new File(dir, sFileName).delete();
        }
        else if (rw.equals("read") && !sFileName.equals("")) {
            try {
                InputStream inputStream = new FileInputStream(new File(dir, sFileName));
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String strRead;
                while ((strRead = bufferedReader.readLine()) != null) {
                    arrayOfString = Arrays.copyOf(arrayOfString, arrayOfString.length + 1);
                    arrayOfString[arrayOfString.length - 1] = strRead;
                }
                inputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
            } catch (Exception e) {
            }
        }
        else if (rw.equals("read") && sFileName.equals("")) {
            arrayOfString = dir.list();
        }

        return arrayOfString;
    }

    public void createSurface() {
        RelativeLayout.LayoutParams foldLinParam = new RelativeLayout.LayoutParams(new RelativeLayout.LayoutParams((int)(displayWidth),(int)(16*(displayHeight/20))));

        mainDisplayGrid = new GridLayout(this);
        mainDisplayGrid.setUseDefaultMargins(true);
        mainDisplayGrid.setLayoutParams(foldLinParam);
        mainDisplayGrid.setRowCount(1);
        mainDisplayGrid.setColumnCount(4);
        mainDisplayGrid.setX((float) (20 *xfact));
        mainDisplayGrid.setY((float) (displayHeight/6 *yfact));
        mainRelDisplay.addView(mainDisplayGrid);

        scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        scrollView.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth/5, (int)(16*(displayHeight/20))));
        scrollView.setVerticalScrollBarEnabled(false);


        scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollBy(0, basescrPosY);
            }
        });

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                basescrPosY = scrollY;
            }
        });

        RelativeLayout.LayoutParams headRelParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int)(displayHeight/6 *yfact));
        headRelParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        headMScroll = new HorizontalScrollView(this);
        headMScroll.setHorizontalScrollBarEnabled(false);
        headMScroll.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        headMScroll.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        headMScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                    fileBrowser.fragmentShutdown(fileBrowser.showList,3);
            }
        });
        headMenueIcon = new ImageView[0];

        String[] headMenueIconsStringArray = file_icon_Loader("Icons/headMenueIcons");
        final LinearLayout headMenue = new LinearLayout(this);
        headMenue.setOrientation(LinearLayout.HORIZONTAL);
        headMenue.setLayoutParams(headRelParam);
        headMenue.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        headMenue.setX((float) (20 *xfact));
        headMenue.setY((float) (40 * yfact));

        headMScroll.addView(headMenue);
        mainRelDisplay.addView(headMScroll);

        for (int i = 0; i < headMenueIconsStringArray.length; i++) {
            headMenueIcon = Arrays.copyOf(headMenueIcon, headMenueIcon.length + 1);
            headMenueIcon[headMenueIcon.length - 1] = new ImageView(this);
            headMenueIcon[headMenueIcon.length - 1].setImageBitmap(bitmapLoader("Icons/headMenueIcons/" + headMenueIconsStringArray[i]));
            headMenueIcon[headMenueIcon.length - 1].setTag("headMenue " + headMenueIconsStringArray[i]);
            headMenueIcon[headMenueIcon.length - 1].setLayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth/14 * xfact), (int) (displayWidth/14 * xfact)));
            headMenueIcon[headMenueIcon.length - 1].setPadding(10,0,10,0);

            if(headMenueIconsStringArray[i].contains("Downloads"))
                headMenueIcon[headMenueIcon.length - 1].setEnabled(false);

            headMenue.addView(headMenueIcon[headMenueIcon.length - 1]);
            if (!headMenueIconsStringArray[i].contains("Empty"))
                headMenueIcon[headMenueIcon.length - 1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String tag = view.getTag().toString().substring(view.getTag().toString().indexOf(" ") + 1);
                        if(!(tag.contains("mail")||tag.contains("online"))) {
                            if (view.getTag().toString().contains("closed")) {
                                tag = tag.replace("closed", "open");
                                view.setTag(view.getTag().toString().replace("closed", "open"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/headMenueIcons/" + tag));
                            } else if (view.getTag().toString().contains("open")) {
                                tag = tag.replace("open", "closed");
                                view.setTag(view.getTag().toString().replace("open", "closed"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/headMenueIcons/" + tag));
                            }
                        } else {
                            if (view.getTag().toString().contains("closed")) {
                                tag = tag.replace("closed", "open");
                                view.setTag(view.getTag().toString().replace("closed", "open"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/headMenueIcons/" + tag));
                            } else if (view.getTag().toString().contains("open")) {
                                tag = tag.replace("open", "running");
                                view.setTag(view.getTag().toString().replace("open", "running"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/headMenueIcons/" + tag));
                                changeIcon(headMenueIcon02[2],"sideRightMenueIcons","closed","open");
                                headMenueIcon02[2].setEnabled(true);
                            } else if (view.getTag().toString().contains("running")) {
                                tag = tag.replace("running", "closed");
                                view.setTag(view.getTag().toString().replace("running", "closed"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/headMenueIcons/" + tag));
                                changeIcon(headMenueIcon02[2],"sideRightMenueIcons","open","closed");
                                headMenueIcon02[2].setEnabled(false);
                                fileBrowser.fragId = 0;
                                return;
                            }
                        }
                        if (tag.contains("Edit_open")) {
                            if (showList != null && showList.isVisible()) {
                                closeListlinkedIcons(new ImageView[]{headMenueIcon[5], headMenueIcon[6], headMenueIcon[7],headMenueIcon01[1], headMenueIcon02[3],headMenueIcon02[5]},
                                        new String[] { "headMenueIcons", "headMenueIcons", "headMenueIcons", "sideLeftMenueIcons", "sideRightMenueIcons", "sideRightMenueIcons"});
                                fragmentShutdown(showList, 3);
                            }
                            if(!(urldevice == null || urldevice.equals(""))) {
                                int wf = 4;
                                if(yfact <= 0.625) wf = 3;

                                int[] iconpos = new int[2];
                                headMenueIcon[2].getLocationOnScreen(iconpos);

                                calledBy = "Edit-Search";
                                createList("startList", 1, "Language/" + language + "/Edit_Tx.txt", 8, iconpos[0] +view.getWidth(),
                                        iconpos[1] +headMenueIcon[2].getHeight()/2, displayWidth /wf, "ru");
                            }
                            else {
                                closeListlinkedIcons(new ImageView[]{headMenueIcon[2], headMenueIcon01[1], headMenueIcon02[3]}, new String[] {"headMenueIcons", "sideLeftMenueIcons", "sideRightMenueIcons"});
                                if (showList != null && showList.isVisible())
                                    fragmentShutdown(showList, 3);
                            }
                        } else if (tag.contains("Edit_closed")) {
                            if (showList != null && showList.isVisible())
                                fragmentShutdown(showList, 3);
                        } else if (tag.contains("mail_open")) {
                            if (showList != null && showList.isVisible()) {
                                closeListlinkedIcons(new ImageView[]{headMenueIcon[2], headMenueIcon[6], headMenueIcon[7],headMenueIcon01[1], headMenueIcon02[3]},
                                        new String[] { "headMenueIcons", "headMenueIcons", "headMenueIcons", "sideLeftMenueIcons", "sideRightMenueIcons"});
                                fragmentShutdown(showList, 3);
                            }
                            fragmentStart(createSendEmail, 5,"emailDisplay", null, 1, 1,
                                    displayWidth -2, displayHeight -2);

                        } else if (tag.contains("mail_closed")) {
                            if(openFrags.equals("")) {
                                closeListlinkedIcons(new ImageView[]{headMenueIcon02[2], headMenueIcon[5]}, new String[]{"sideRightMenueIcons",
                                        "headMenueIcons"});
                                headMenueIcon02[2].setEnabled(false);
                            } else
                                closeListlinkedIcons(new ImageView[]{headMenueIcon[5]}, new String[]{"headMenueIcons"});
                            if (createSendEmail != null && createSendEmail.isVisible())
                                fragmentShutdown(createSendEmail, 5);

                        } else if(tag.contains("mail_running")) {
                            fragId = 5;
                            fileBrowser.startMovePanel(fragId);
                        } else if (tag.contains("online")) {
                            if(tag.endsWith("_open.png")) {
                                if (showList != null && showList.isVisible()) {
                                    closeListlinkedIcons(new ImageView[]{headMenueIcon[2], headMenueIcon[5], headMenueIcon[7],headMenueIcon01[1], headMenueIcon02[3]},
                                            new String[] { "headMenueIcons", "headMenueIcons", "headMenueIcons", "sideLeftMenueIcons", "sideRightMenueIcons"});
                                    fragmentShutdown(showList, 3);
                                }
                                if(haveNetwork()) {
                                    Bundle bund = new Bundle();
                                    bund.putString("URL", searchMashineUrl);
                                    fragmentStart(webBrowserDisplay, 8, "webBrowserDisplay", bund, 1, 1,
                                            displayWidth -2, displayHeight -2);
                                } else {
                                    fileBrowser.messageStarter("mailNoInternet", docu_Loader("Language/" + language + "/NoInternet_avaliable.txt"),
                                            6000);
                                    changeIcon(view,"headMenueIcons","open","closed");
                                    changeIcon(view,"headMenueIcons","running","closed");
                                }
                            } else if(tag.endsWith("_closed.png")) {
                                if(fileBrowser.webBrowserDisplay != null && fileBrowser.webBrowserDisplay.isVisible())
                                    fileBrowser.fragmentShutdown(webBrowserDisplay, 8);
                                if(openFrags.equals("")) {
                                    closeListlinkedIcons(new ImageView[]{headMenueIcon02[2], headMenueIcon[6]}, new String[]{"sideRightMenueIcons",
                                            "headMenueIcons"});
                                    headMenueIcon02[2].setEnabled(false);
                                } else
                                    closeListlinkedIcons(new ImageView[]{headMenueIcon[6]}, new String[]{"headMenueIcons"});
                            } else if(tag.endsWith("_running.png")) {
                                fragId = 8;
                                fileBrowser.startMovePanel(fragId);
                            }

                        } else if (tag.contains("searchMashine")) {

                            if (tag.contains("open")) {
                                if (showList != null && showList.isVisible()) {
                                    closeListlinkedIcons(new ImageView[]{headMenueIcon[2], headMenueIcon[5], headMenueIcon[6],headMenueIcon01[1], headMenueIcon02[3], headMenueIcon02[5]},
                                            new String[] { "headMenueIcons", "headMenueIcons", "headMenueIcons", "sideLeftMenueIcons", "sideRightMenueIcons", "sideRightMenueIcons"});
                                    fragmentShutdown(showList, 3);
                                }

                                int wf = 6;
                                if(yfact <= 0.625) wf = 4;

                                int[] iconpos = new int[2];
                                headMenueIcon[7].getLocationOnScreen(iconpos);

                                createList("searchMachineList",1,"Language/" +language+ "/SearchMachineList.txt",6,
                                        iconpos[0],iconpos[1] +headMenueIcon[7].getHeight()/2,displayWidth/wf,"lu");


                            } else if(tag.contains("closed")) {
                                threadStop = true;
                                if(showList != null && showList.isVisible())
                                    fragmentShutdown(showList, 3);
                            }
                        }

                    }

                });
        }

        headMenueIcon01 = new ImageView[0];
        String[] sideLeftMenueStringArray = file_icon_Loader("Icons/sideLeftMenueIcons");
        LinearLayout sideLeftMenue = new LinearLayout(this);
        sideLeftMenue.setOrientation(LinearLayout.VERTICAL);
        sideLeftMenue.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        sideLeftMenue.setLayoutParams(new RelativeLayout.LayoutParams((int)(displayWidth/10 * xfact), (int)(16*displayHeight/20)));

        for (int i = 0; i < sideLeftMenueStringArray.length; i++) {
            headMenueIcon01 = Arrays.copyOf(headMenueIcon01, headMenueIcon01.length + 1);
            headMenueIcon01[headMenueIcon01.length - 1] = new ImageView(this);
            headMenueIcon01[headMenueIcon01.length - 1].setLayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth/10 * xfact), (int) (displayWidth/10 * xfact)));
            headMenueIcon01[headMenueIcon01.length - 1].setTag(headMenueIcon01.length - 1 + " " + sideLeftMenueStringArray[i]);

            if(sideLeftMenueStringArray[i].contains("Trash")) {
                String[] st = read_writeFileOnInternalStorage("read", "TrashIndex", "", "");

                if (st != null && st.length > 0) {
                    for(String s:st)
                        System.err.println("..."+s+"...");
                    headMenueIcon01[headMenueIcon01.length - 1].setTag(headMenueIcon01[headMenueIcon01.length - 1].getTag().
                            toString().replace("closed","open"));
                    sideLeftMenueStringArray[i] = sideLeftMenueStringArray[i].replace("closed", "open");
                } else
                    headMenueIcon01[headMenueIcon01.length - 1].setEnabled(false);
            }

            headMenueIcon01[headMenueIcon01.length - 1].setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + sideLeftMenueStringArray[i]));

            sideLeftMenue.addView(headMenueIcon01[headMenueIcon01.length - 1]);
            if (!sideLeftMenueStringArray[i].contains("Empty") && !sideLeftMenueStringArray[i].contains("Kopie"))
                headMenueIcon01[headMenueIcon01.length - 1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String tag = view.getTag().toString().substring(view.getTag().toString().indexOf(" ") + 1);
                        int pos = Integer.parseInt(view.getTag().toString().substring(0, view.getTag().toString().indexOf(" ")));

                        if (view.getTag().toString().contains("closed")) {

                            if (tag.contains("Device")) {
                                tag = tag.replace("closed", "open");
                                view.setTag(view.getTag().toString().replace("closed", "open"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + tag));

                                if (showList != null && showList.isVisible()) {
                                    closeListlinkedIcons(new ImageView[]{headMenueIcon[2],headMenueIcon[5], headMenueIcon[6], headMenueIcon[7],headMenueIcon02[3]},
                                            new String[] { "headMenueIcons", "headMenueIcons", "headMenueIcons", "headMenueIcons", "sideRightMenueIcons"});
                                    fragmentShutdown(showList, 3);
                                }
                                createList_systemUrl(2, 4);
                            } else if (tag.contains("IVor") && filebrowser_01 != null && filebrowser_01.isVisible() &&
                                    headMenueIcon01[pos + 2].getTag().toString().contains("closed")) {
                                tag = tag.replace("closed", "open");
                                view.setTag(view.getTag().toString().replace("closed", "open"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + tag));
                                headMenueIcon01[pos + 1].setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + headMenueIcon01[pos + 1].getTag().toString().substring(
                                        headMenueIcon01[pos + 1].getTag().toString().indexOf(" ") + 1).replace("closed", "open")));
                                headMenueIcon01[pos + 1].setTag(headMenueIcon01[pos + 1].getTag().toString().replace("closed", "open"));
                                headMenueIcon01[pos + 2].setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + headMenueIcon01[pos + 2].getTag().toString().substring(
                                        headMenueIcon01[pos + 2].getTag().toString().indexOf(" ") + 1).replace("open", "closed")));
                                headMenueIcon01[pos + 2].setTag(headMenueIcon01[pos + 2].getTag().toString().replace("open", "closed"));

                                // make memory
                                memoryMainscrPosX = mainscrPosX;
                                memoryDetscrPosY = detscrPosY;
                                memoryBasescrPosY = basescrPosY;
                                memoryDevicePath = devicePath;
                                memoryUrlDevice = urldevice;
                                memorySelectedTx = selectedTx;

                            } else if (tag.contains("RZur") && !memoryDevicePath.equals("")) {
                                tag = tag.replace("closed", "open");
                                view.setTag(view.getTag().toString().replace("closed", "open"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + tag));
                                headMenueIcon01[pos - 1].setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + headMenueIcon01[pos - 1].getTag().toString().substring(
                                        headMenueIcon01[pos - 1].getTag().toString().indexOf(" ") + 1).replace("open", "closed")));
                                headMenueIcon01[pos - 1].setTag(headMenueIcon01[pos - 1].getTag().toString().replace("open", "closed"));
                                headMenueIcon01[pos - 2].setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + headMenueIcon01[pos - 2].getTag().toString().substring(
                                        headMenueIcon01[pos - 2].getTag().toString().indexOf(" ") + 1).replace("open", "closed")));
                                headMenueIcon01[pos - 2].setTag(headMenueIcon01[pos - 2].getTag().toString().replace("open", "closed"));

                                //recover memoryTasks
                                urldevice = memoryUrlDevice;
                                devicePath = memoryDevicePath;
                                detscrPosY = memoryDetscrPosY;
                                mainscrPosX = memoryMainscrPosX;
                                basescrPosY = memoryBasescrPosY;
                                selectedTx = memorySelectedTx;
                                createList_systemUrl(2, 4);
                                scrollView.removeAllViews();
                                String url = devicePath,
                                        urldevicePath = url.substring(urldevice.length() + 1);

                                paramList = fileBrowser.createArrayList(urldevicePath);
                                createFolder(urldevice);

                                fragmentStart(filebrowser_01, 1, "fileBrowser01", null, (int) (270 * xfact), (int) (440 * yfact), (int) (4 * displayWidth / 5 - 80 * yfact), (int) (5 * displayHeight / 7));

                                //clean up
                                memoryParamList = null;
                                memoryMainscrPosX = 0;
                                memoryBasescrPosY = 0;
                                memorySelectedTx = 0;
                                memoryDevicePath = "";
                                memoryUrlDevice = "";

                                timeImage.setVisibility(View.VISIBLE);
                                timerAnimation.start();
                                //
                            } else if (tag.contains("Trash")) {
                                tag = tag.replace("closed", "open");
                                view.setTag(view.getTag().toString().replace("closed", "open"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + tag));

                            }

                        } else if (view.getTag().toString().contains("open") && (tag.contains("Device") || tag.contains("Trash"))) {

                            if (tag.contains("Trash")) {

                                int[] iconpos = new int[2], listpos = new int[2];
                                headMenueIcon01[7].getLocationOnScreen(iconpos);

                                if (showList != null && showList.isVisible()) {
                                    frameLy.get(3).getLocationOnScreen(listpos);
                                    listpos[1] = listpos[1] - frameLy.get(3).getHeight();

                                    closeListlinkedIcons(new ImageView[]{headMenueIcon[2],headMenueIcon[5], headMenueIcon[6], headMenueIcon[7],headMenueIcon01[1], headMenueIcon02[3],
                                                    headMenueIcon02[5]},
                                            new String[] { "headMenueIcons", "headMenueIcons", "headMenueIcons", "headMenueIcons", "sideLeftMenueIcons", "sideRightMenueIcons",
                                                    "sideRightMenueIcons"});
                                    fragmentShutdown(showList, 3);
                                }
                                if(listpos[0] == 0  || (listpos[0] != iconpos[0] + (int) (headMenueIcon01[7].getWidth() + 10) &&
                                        listpos[1] != iconpos[1] - (int) (headMenueIcon01[7].getHeight() + 10))) {

                                    float f = 4;
                                    if (yfact < 0.625)
                                        f = 3;


                                    fileBrowser.createList("TrashList", 1, "Language/" + fileBrowser.language + "/TrashList.txt", 6,
                                            iconpos[0] + (int) (headMenueIcon01[7].getWidth() + 10), iconpos[1] - (int) (headMenueIcon01[7].getHeight() + 10),
                                            (int) (displayWidth / f), "ru");

                                    fileBrowser.frameLy.get(3).bringToFront();
                                    closeListlinkedIcons(new ImageView[]{headMenueIcon[2], headMenueIcon01[1], headMenueIcon02[3]},
                                            new String[]{"headMenueIcons", "sideLeftMenueIcons", "sideRightMenueIcons"});
                                }

                                return;

                            } else {
                                tag = tag.replace("open", "closed");
                                view.setTag(view.getTag().toString().replace("open", "closed"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideLeftMenueIcons/" + tag));

                                if (showList != null && showList.isVisible()) {
                                    fragmentShutdown(showList, 3);
                                    closeListlinkedIcons(new ImageView[]{headMenueIcon01[1]},
                                            new String[]{"sideLeftMenueIcons"});
                                }
                            }
                        }
                    }
                });
        }
        folderPanelFrame = new RelativeLayout(this);
        folderPanelFrame.setLayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth / 6 * xfact), 16*displayHeight/20));
        folderPanelFrame.setPadding(5,5,5,5);
        gridMiddleFrame = new RelativeLayout(this);
        gridMiddleFrame.setLayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth -(2*displayWidth/10 +displayWidth / 6) * xfact), 16*displayHeight/20));

        mainDisplayGrid.addView(sideLeftMenue);
        mainDisplayGrid.addView(folderPanelFrame);
        mainDisplayGrid.addView(gridMiddleFrame);
        mainDisplayGrid.addView(createSideRightMenue());
    }

    public LinearLayout createSideRightMenue() {
        RelativeLayout.LayoutParams mainRelParams = new RelativeLayout.LayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth / 10 *xfact),
                (int) (18 * displayHeight / 20)));

        hide = 1;
        String[] sideRightMenueStringArray = fileBrowser.file_icon_Loader("Icons/sideRightMenueIcons");
        LinearLayout sideRightMenue = new LinearLayout(fileBrowser);
        sideRightMenue.setOrientation(LinearLayout.VERTICAL);
        sideRightMenue.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        sideRightMenue.setLayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth / 10 *xfact), (int) (16 * displayHeight / 20)));

        headMenueIcon02 = new ImageView[0];

        for (int i = 0; i < sideRightMenueStringArray.length; i++) {
            if (sideRightMenueStringArray[i].startsWith("userPermission"))
                sideRightMenueStringArray[i] = sideRightMenueStringArray[i].replace("closed", "open");

            headMenueIcon02 = Arrays.copyOf(headMenueIcon02, headMenueIcon02.length + 1);
            headMenueIcon02[headMenueIcon02.length - 1] = new ImageView(fileBrowser);
            headMenueIcon02[headMenueIcon02.length - 1].setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + sideRightMenueStringArray[i]));
            if (sideRightMenueStringArray[i].contains(fileBrowser.language))
                headMenueIcon02[headMenueIcon02.length - 1].setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + sideRightMenueStringArray[i].replace("closed", "open")));
            headMenueIcon02[headMenueIcon02.length - 1].setLayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth/10 * xfact), (int) (displayWidth/10 * xfact)));
            headMenueIcon02[headMenueIcon02.length - 1].setTag(headMenueIcon02.length - 1 + " " + sideRightMenueStringArray[i]);
            if (sideRightMenueStringArray[i].contains("mediaBack"))
                headMenueIcon02[headMenueIcon02.length - 1].setEnabled(false);
            if (sideRightMenueStringArray[i].startsWith("Hided")) {
                HidedView = headMenueIcon02[headMenueIcon02.length - 1];
                headMenueIcon02[headMenueIcon02.length - 1].setEnabled(false);
            }
            sideRightMenue.addView(headMenueIcon02[headMenueIcon02.length - 1]);
            if (!sideRightMenueStringArray[i].contains("Empty"))
                headMenueIcon02[headMenueIcon02.length - 1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String tag = view.getTag().toString().substring(view.getTag().toString().indexOf(" ") + 1);

                        if (view.getTag().toString().contains("closed")) {
                            if (tag.contains("mediaList")) {
                                if (devicePath == null || devicePath.substring(devicePath.lastIndexOf("/") + 1).contains(".")) {
                                    fileBrowser.messageStarter("NoFileSelected", docu_Loader("Language/" + language + "/Instruction_MediaList.txt"),
                                            8500);
                                    return;
                                } else {
                                    if (showList != null && showList.isVisible()) {
                                        closeListlinkedIcons(new ImageView[]{headMenueIcon[2],headMenueIcon[5], headMenueIcon[6], headMenueIcon[7],headMenueIcon01[1],headMenueIcon02[5]},
                                                new String[] { "headMenueIcons", "headMenueIcons", "headMenueIcons", "headMenueIcons", "sideLeftMenueIcons", "sideRightMenueIcons"});
                                        fragmentShutdown(showList, 3);
                                    }
                                    if (fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible())
                                        fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);

                                    float f = 4;
                                    if(yfact <= 0.625)
                                        f = 3;

                                    int[] iconpos = new int[2];
                                    headMenueIcon02[3].getLocationOnScreen(iconpos);

                                    fileBrowser.createList("mediaCollectionList", 1, "Language/" + language + "/Index_MediaList.txt", 3,
                                            iconpos[0], iconpos[1], (int)(displayWidth / f), "lu");
                                }
                            } else if (tag.contains("vLogo")) {
                                if (showList != null && showList.isVisible()) {
                                    closeListlinkedIcons(new ImageView[]{headMenueIcon[2],headMenueIcon[5], headMenueIcon[6], headMenueIcon[7],headMenueIcon01[1],
                                                    headMenueIcon02[3],
                                            },
                                            new String[] { "headMenueIcons", "headMenueIcons", "headMenueIcons", "headMenueIcons", "sideLeftMenueIcons",
                                                    "sideRightMenueIcons"});
                                    fragmentShutdown(showList, 3);
                                }
                                float f = 4;
                                if(yfact <= 0.625)
                                    f = 3;
                                int[] iconpos = new int[2];
                                headMenueIcon02[5].getLocationOnScreen(iconpos);

                                fileBrowser.createList("documentList", 1, "Language/" + language + "/Index_DocumentList.txt", 3,
                                        iconpos[0], iconpos[1], (int)(displayWidth / f), "lu");

                            } else if ((tag.contains("E@sySoft"))) {
                                fileBrowser.timeImage.setVisibility(View.VISIBLE);
                                fileBrowser.timerAnimation.start();
                                view.setEnabled(false);
                                fileBrowser.messageStarter("InfoContact", docu_Loader("Language/" + language + "/InfoContact.txt"),
                                        8000);

                            } else if ((tag.contains("xQR_BarCode"))) {
                                try{
                                    Intent qrBarCode = new Intent(getPackageManager().getLaunchIntentForPackage("easysoft.cooperation.qrbarcode"));
                                    qrBarCode.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(qrBarCode);
                                    fileBrowser.finish();
                                } catch (Exception e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/hewogithub63/e-sySoft.freeFileBrowser/tree/main/release/")));
                                }

                            }

                            if(!(tag.contains("vLogo") || tag.contains("mediaList") || tag.contains("mediaList"))) {
                                tag = tag.replace("closed", "open");
                                view.setTag(view.getTag().toString().replace("closed", "open"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));

                            } else {
                                if (view.getTag().toString().contains("closed")) {
                                    tag = tag.replace("closed", "open");
                                    view.setTag(view.getTag().toString().replace("closed", "open"));
                                    ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));
                                } else if (view.getTag().toString().contains("running")) {
                                    tag = tag.replace("running", "closed");
                                    view.setTag(view.getTag().toString().replace("running", "closed"));
                                    ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));
                                }
                            }

                            if (tag.startsWith("Hided")) {
                                hide = 0;
                                String url = devicePath,
                                        urldevicePath = "";
                                if (!url.equals(urldevice))
                                    urldevicePath = url.substring(urldevice.length() + 1);

                                paramList = fileBrowser.createArrayList(urldevicePath);

                                if (urldevicePath.length() != 0 || urldevicePath.contains("/")) {
                                    fileBrowser.createFolder(fileBrowser.urldevice);
                                    fileBrowser.fragmentStart(fileBrowser.filebrowser_01, 1, "fileBrowser01", null, (int) (250 * xfact), (int) (440 * yfact),
                                            (int) (4 * displayWidth / 5 - 80 * yfact), (int) (5 * displayHeight / 7));
                                } else
                                    fileBrowser.createFolder(fileBrowser.urldevice);
                            }
                        } else if (view.getTag().toString().contains("open")) {
                            if (tag.startsWith("Hided")) {
                                tag = tag.replace("open", "closed");

                                view.setTag(view.getTag().toString().replace("open", "closed"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));

                                if (devicePath.contains("/."))
                                    devicePath = devicePath.substring(0, devicePath.indexOf("/."));
                                hide = 1;
                                String url = devicePath,
                                        urldevicePath = urldevice;
                                if (!url.equals(urldevice))
                                    urldevicePath = url.substring(urldevice.length() + 1);

                                paramList = fileBrowser.createArrayList(urldevicePath);


                                if (urldevicePath.length() != 0 || urldevicePath.contains("/")) {
                                    fileBrowser.createFolder(fileBrowser.urldevice);
                                    fileBrowser.fragmentStart(fileBrowser.filebrowser_01, 1, "fileBrowser01", null, (int) (250 * xfact), (int) (440 * yfact),
                                            (int) (4 * displayWidth / 5 - 80 * yfact), (int) (5 * displayHeight / 7));
                                } else
                                    fileBrowser.createFolder(fileBrowser.urldevice);

                            } else if (tag.startsWith("mediaBack")) {
                                view.setEnabled(false);

                                if ((fileBrowser.createSendEmail != null && fileBrowser.createSendEmail.isVisible())) {
                                    if (fileBrowser.createSendEmail.attachedList != null && fileBrowser.createSendEmail.attachment &&
                                            devicePath.substring(devicePath.lastIndexOf("/") + 1).contains(".")) {
                                        fileBrowser.createSendEmail.attachedList.add(new String[]{devicePath.substring(0, devicePath.lastIndexOf("/")), devicePath.substring(devicePath.lastIndexOf("/") + 1)});
                                    }

                                }
                                fileBrowser.startMovePanel(fragId);

                            } else if (tag.startsWith("mediaList")) {
                                if(openFrags.equals("")) {
                                    closeListlinkedIcons(new ImageView[]{headMenueIcon02[2], headMenueIcon02[3]}, new String[]{"sideRightMenueIcons",
                                            "sideRightMenueIcons"});
                                    headMenueIcon02[2].setEnabled(false);
                                } else {
                                    changeIcon(headMenueIcon02[3], "sideRightMenueIcons", "open", "running");
                                }


                                if (fileBrowser.showMediaDisplay != null && fileBrowser.showMediaDisplay.videoView.isPlaying()) {
                                    if(fileBrowser.runningMediaList != null && fileBrowser.runningMediaList.size() > 0) {
                                        if (fileBrowser.showMediaDisplay.kindOfMedia.equals("AUDIO")) {
                                            fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "open", "closed");
                                            fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "open");
                                            if (openFrags.equals("")) {
                                                fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                                                fileBrowser.headMenueIcon02[2].setEnabled(false);
                                            }

                                            runningMediaList = new ArrayList<>(0);

                                            fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "running", "open");
                                            fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "running", "open");
                                            fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "open");
                                            fileBrowser.showMediaDisplay.mP.stop();
                                            fileBrowser.showMediaDisplay.mP.reset();
                                            fileBrowser.showMediaDisplay.mP.release();

                                            fileBrowser.fragmentShutdown(fileBrowser.showMediaDisplay, 4);
                                            fileBrowser.intendStarted = false;
                                            selectedFile.remove(selectedFile.size() -1);
                                            devicePath = devicePath.substring(0,devicePath.lastIndexOf("/"));
                                            fileBrowser.reloadFileBrowserDisplay();

                                        } else if (fileBrowser.showMediaDisplay.kindOfMedia.equals("VIDEO")) {
                                            tag = tag.replace("open", "running");
                                            view.setTag(view.getTag().toString().replace("open", "running"));
                                            ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));

                                            fragId = 4;
                                            fileBrowser.startMovePanel(fragId);

                                        }
                                        tag = tag.replace("open", "closed");
                                        view.setTag(view.getTag().toString().replace("open", "closed"));

                                    } else if(fileBrowser.showMediaDisplay.kindOfMedia.equals("VIDEO")) {
                                        tag = tag.replace("open", "running");
                                        view.setTag(view.getTag().toString().replace("open", "running"));
                                        fragId = 4;
                                        ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));
                                        fileBrowser.startMovePanel(fragId);
                                    } else {
                                        tag = tag.replace("openOne", "closed");
                                        view.setTag(view.getTag().toString().replace("openOne", "closed"));
                                        ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));

                                        fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "running", "open");
                                        fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "running", "open");
                                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "open");

                                        fileBrowser.showMediaDisplay.mP.stop();
                                        fileBrowser.showMediaDisplay.mP.reset();
                                        fileBrowser.showMediaDisplay.mP.release();

                                        fileBrowser.fragmentShutdown(fileBrowser.showMediaDisplay, 4);
                                        fileBrowser.intendStarted = false;
                                        selectedFile.remove(selectedFile.size() -1);
                                        devicePath = devicePath.substring(0,devicePath.lastIndexOf("/"));
                                        fileBrowser.reloadFileBrowserDisplay();
                                    }

                                } else {
                                    tag = tag.replace("openOne", "closed");
                                    tag = tag.replace("open", "closed");
                                    view.setTag(view.getTag().toString().replace("openOne", "closed"));
                                    view.setTag(view.getTag().toString().replace("open", "closed"));
                                    ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));
                                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                                        fileBrowser.fragmentShutdown(showList,3);
                                    fileBrowser.fragmentShutdown(fileBrowser.showMediaDisplay, 4);

                                }

                            } else if (tag.contains("vLogo")) {
                                if(createTxEditor != null && createTxEditor.isVisible()) {
                                    fileBrowser.isPdf = false;
                                    tag = tag.replace("open", "running");

                                    view.setTag(view.getTag().toString().replace("open", "running"));
                                    ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));

                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "running", "open");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "running", "open");

                                    fragId = 7;
                                    fileBrowser.startMovePanel(fragId);

                                    if (showList != null && showList.isVisible()) {
                                        closeListlinkedIcons(new ImageView[]{headMenueIcon[2], headMenueIcon[5], headMenueIcon[6], headMenueIcon[7], headMenueIcon01[1]
                                                },
                                                new String[]{"headMenueIcons", "headMenueIcons", "headMenueIcons", "headMenueIcons", "sideLeftMenueIcons"});
                                        fragmentShutdown(showList, 3);
                                    }
                                } else {
                                    tag = tag.replace("open", "closed");
                                    view.setTag(view.getTag().toString().replace("open", "closed"));
                                    ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));
                                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                                        fileBrowser.fragmentShutdown(showList,3);
                                }

                            } else if ((tag.contains("xQR_BarCode"))) {
                                tag = tag.replace("open", "closed");
                                view.setTag(view.getTag().toString().replace("open", "closed"));
                                ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));

                            }
                        } else if (view.getTag().toString().contains("running")) {

                            if (tag.contains("vLogo")) {
                                fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "closed");

                                fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                                fileBrowser.headMenueIcon02[2].setEnabled(false);


                                fragId = 7;
                            } else if (tag.startsWith("mediaList")) {

                                if(!openFrags.contains("4")) {
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                                    fileBrowser.headMenueIcon02[2].setEnabled(false);

                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "runningOne", "closed");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "closed");

                                } else {
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                                    fileBrowser.headMenueIcon02[2].setEnabled(false);

                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "runningOne", "closed");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "closed");

                                }

                                fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "open");
                                fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "running", "open");
                                fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "running", "open");

                                if (fileBrowser.showMediaDisplay.videoView.isPlaying()) {
                                    if(fileBrowser.runningMediaList != null && fileBrowser.runningMediaList.size() > 0) {

                                        tag = tag.replace("running", "closed");
                                        view.setTag(view.getTag().toString().replace("running", "closed"));

                                        runningMediaList = new ArrayList<>(0);

                                    } else {
                                        tag = tag.replace("runningOne", "closed");
                                        view.setTag(view.getTag().toString().replace("runningOne", "closed"));

                                    }
                                    ((ImageView) view).setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" + tag));
                                    fileBrowser.showMediaDisplay.mP.stop();
                                    fileBrowser.showMediaDisplay.mP.reset();
                                    fileBrowser.showMediaDisplay.mP.release();
                                    fileBrowser.fragmentShutdown(fileBrowser.showMediaDisplay, 4);

                                    fileBrowser.intendStarted = false;
                                    selectedFile.remove(selectedFile.size() -1);
                                    devicePath = devicePath.substring(0,devicePath.lastIndexOf("/"));
                                    fileBrowser.reloadFileBrowserDisplay();
                                }
                                fragId = 4;
                            }
                        }

                    }
                });
        }
        return sideRightMenue;
    }

    public void closeListlinkedIcons(ImageView[] iconViews, String[] iconType) {

            fileBrowser.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int i=0;
                    for (ImageView iconView : iconViews) {
                        iconView.setTag(iconView.getTag().toString().replace("openOne", "closed"));
                        iconView.setTag(iconView.getTag().toString().replace("open", "closed"));
                        String iconName = iconView.getTag().toString().substring(iconView.getTag().toString().indexOf(" ") + 1);
                        iconView.setImageBitmap(bitmapLoader("Icons/" + iconType[i] + "/" + iconName));
                        i++;
                    }
                }
            });

    }
    public void changeIcon(View v, String folder, String from, String to) {
        v.setTag(v.getTag().toString().replace(from, to));
        String dir = "Icons/"+folder;
        if(folder.startsWith("./"))
            dir = folder.substring(folder.indexOf("./")+2);
        final String dirs = dir;

            fileBrowser.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        ((ImageView) v).setImageBitmap(fileBrowser.bitmapLoader(dirs+"/" + v.getTag().toString().substring(v.getTag().toString().indexOf(" ") + 1)));
                        if(calledBy.equals("toTrash"))
                            fileBrowser.headMenueIcon01[fileBrowser.headMenueIcon01.length - 1].setEnabled(true);
                        else if(calledBy.equals("fromTrash"))
                            fileBrowser.headMenueIcon01[fileBrowser.headMenueIcon01.length - 1].setEnabled(false);

                    } catch (ClassCastException cE) {
                        if (to.equals("open"))
                            ((TextView) v).setTextColor(getResources().getColor(R.color.blue));
                        else if (to.equals("closed"))
                            ((TextView) v).setTextColor(getResources().getColor(R.color.black));
                    }
                    calledBy = "";
                }
            });

    }

    public void createFolder(String device) {
        boolean select = false;

        String[] folderArrayString = paramList.get(0);
        if (folderArrayString != null)
            Arrays.sort(folderArrayString);
        else
            return;
        headMenueIcon03 = new ImageView[folderArrayString.length];

        LinearLayout folderMenueLy = new LinearLayout(this);
        folderMenueLy.setOrientation(LinearLayout.VERTICAL);
        folderMenueLy.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < folderArrayString.length; i++) {
            String docu = "";
            if (folderArrayString[i].substring(1).contains("."))
                docu = "document_closed.png";
            else
                docu = "file_closed.png";

            if(selectedFile != null) {
                for (String s : selectedFile) {
                    if (paramList.get(0)[i].equals(s)) {
                        select = true;
                        docu = docu.replace("closed", "open");

                        break;
                    }
                }
            }

            headMenueIcon03[i] = new ImageView(this);
            if (select) {
                headMenueIcon03[i].setImageBitmap(bitmapLoader("Icons/browserIcons/" + docu));
                headMenueIcon03[i].setTag(i + " " + docu + "  " + device + "/" + folderArrayString[i].replace("closed", "open"));
            } else {
                headMenueIcon03[i].setImageBitmap(bitmapLoader("Icons/browserIcons/" + docu));
                headMenueIcon03[i].setTag(i + " " + docu + "  " + device + "/" + folderArrayString[i]);
            }
            headMenueIcon03[i].setLayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth / 8 * xfact), displayHeight/12));
            if (headMenueIcon03[i].getTag().toString().contains("document"))
                headMenueIcon03[i].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String extProgrUrl = v.getTag().toString().substring(v.getTag().toString().lastIndexOf("  ") + 2);

                        /*if (fileBrowser.intendStarted) {
                            if (fileBrowser.showMediaDisplay != null && fileBrowser.showMediaDisplay.isVisible()) {
                                fileBrowser.showMediaDisplay.createMediaPlay(extProgrUrl);

                            }
                        } else { */

                        if (extProgrUrl.endsWith(".html"))
                            extProgrUrl = "file://" + extProgrUrl;

                        fileBrowser.startExtApp(extProgrUrl);
                        //}
                        devicePath = extProgrUrl;
                        fileBrowser.reloadFileBrowserDisplay();
                        return true;
                    }
                });
            headMenueIcon03[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnClick(v);
                }
            });

            folderMenueLy.addView(headMenueIcon03[i]);

            TextView folderTx = new TextView(this);
            folderTx.setText(folderArrayString[i]);
            folderTx.setTextSize((float) (textSize));
            folderTx.setBackgroundColor(getResources().getColor(R.color.white_overlay));
            folderTx.setTextColor(getResources().getColor(R.color.white));
            if (select) {
                folderTx.setTextColor(getResources().getColor(R.color.green));
                select = false;
            }
            folderTx.setX((float) (textSize));

            //TxSplitting

            String st = folderArrayString[i], st1 = "", st2 = "";
            String[] sc = new String[]{" ", "_", "-", "#", "*", "§", "$", "%", "&"};

            while (((float) (st.length() / 12)) >= 1.0) {
                int n = 0, n1 = 0;
                st1 = st.substring(0, 12);
                for (String c : sc) {
                    if (st1.substring(6).contains(c)) {
                        n1++;
                        break;
                    }
                    n++;
                }
                if (n1 > 0) {
                    st1 = st1.substring(0, 6 + st1.substring(6).indexOf(sc[n]) +1);
                    st2 = st2 + st1 + "\n";
                } else
                    st2 = st2 + st1 + "\n";
                st = st.substring(st1.length());
            }
            st2 = st2 + st;

            folderTx.setText(st2);

            //
            folderMenueLy.addView(folderTx);

        }

        headMenueIcon02[1].setEnabled(true);
        scrollView.removeAllViews();
        scrollView.addView(folderMenueLy);
        if(folderPanelFrame.getChildCount() != 0)
            folderPanelFrame.removeAllViews();

        folderPanelFrame.addView(scrollView);
    }

    public void OnClick(View view) {
        String tag01 =  view.getTag().toString();
        // stop running Mediaplayer
        if (tag01.contains("open") && fileBrowser.intendStarted) {
            if((fileBrowser.fragId == 7 && ((device.endsWith(".txt") || (device.endsWith("pdf"))))) &&
                    fileBrowser.createTxEditor != null && fileBrowser.createTxEditor.isVisible()) {
                if(fileBrowser.headMenueIcon02[5].getTag().toString().contains("running") &&
                        (devicePath.endsWith(".pdf") || devicePath.endsWith(".txt"))) {
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "closed");
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "running", "open");
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "running", "open");
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "open");
                    fileBrowser.fragmentShutdown(fileBrowser.createTxEditor,7);
                } else if(fileBrowser.headMenueIcon02[5].getTag().toString().contains("open") &&
                        (devicePath.endsWith(".pdf") || devicePath.endsWith(".txt"))) {
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "open", "closed");
                    fileBrowser.fragmentShutdown(fileBrowser.createTxEditor,7);
                }
            }
            if ((fileBrowser.fragId == 4 && ((!device.endsWith(".txt") && !device.endsWith("pdf")))) &&
                    fileBrowser.showMediaDisplay != null && fileBrowser.showMediaDisplay.isVisible()) {
                //fileBrowser.showMediaDisplay.disrupt = true;

                if (fileBrowser.runningMediaList != null && fileBrowser.runningMediaList.size() > 0)
                    fileBrowser.runningMediaList = null;

                if(fileBrowser.showMediaDisplay.videoView.isPlaying()) {
                    if(!fileBrowser.headMenueIcon02[3].getTag().toString().contains("One")) {
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "open", "closed");
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "closed");
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                    } else {

                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "openOne", "closed");
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "runningOne", "closed");
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                    }
                    fileBrowser.showMediaDisplay.mP.stop();
                    fileBrowser.showMediaDisplay.mP.reset();
                    fileBrowser.showMediaDisplay.mP.release();
                }

                if(openFrags.equals("")) {
                    fileBrowser.closeListlinkedIcons(new ImageView[]{fileBrowser.headMenueIcon02[2], fileBrowser.headMenueIcon02[3]},
                            new String[]{"sideRightMenueIcons", "sideRightMenueIcons"});
                    fileBrowser.headMenueIcon02[2].setEnabled(false);
                } else
                    fileBrowser.closeListlinkedIcons(new ImageView[]{fileBrowser.headMenueIcon02[3]},
                            new String[]{"sideRightMenueIcons"});
                fileBrowser.fragmentShutdown(fileBrowser.showMediaDisplay,4);

            }
            if(fileBrowser.webBrowserDisplay != null && fileBrowser.webBrowserDisplay.isVisible()) {
                if(openFrags.equals("")) {
                    fileBrowser.closeListlinkedIcons(new ImageView[]{headMenueIcon[5], headMenueIcon02[2]}, new String[]{"headMenueIcons", "sideRightMenueIcons"});
                    headMenueIcon02[2].setEnabled(false);
                } else
                    fileBrowser.closeListlinkedIcons(new ImageView[]{headMenueIcon[5]}, new String[]{"headMenueIcons"});

                fileBrowser.fragmentShutdown(fileBrowser.webBrowserDisplay, 8);
            }

            fileBrowser.intendStarted = false;
        }
        devicePath = view.getTag().toString().substring(view.getTag().toString().lastIndexOf("  ") + 2);

        String tag = view.getTag().toString().substring(view.getTag().toString().indexOf(" ") + 1, view.getTag().toString().indexOf("  "));
        int n = (((Integer.parseInt(view.getTag().toString().substring(0, view.getTag().toString().indexOf(" ")))) + 1) * 2) - 1,
                ip = (Integer.parseInt(view.getTag().toString().substring(0, view.getTag().toString().indexOf(" "))));

        if (tag.contains("closed")) {

            detscrPosY = new Integer[0];
            mainscrPosX = 0;

        } else if (tag.contains("open")) {

            if (filebrowser_01 != null && filebrowser_01.isVisible())
                fragmentShutdown(filebrowser_01, 1);
            devicePath = devicePath.substring(0, devicePath.lastIndexOf("/"));
        }

        reloadFileBrowserDisplay();
        basescrPosY = scrollView.getScrollY();
        devicePath_trans = devicePath;
    }

    public void startExtApp(String Url) {
        if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
            fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
        progrIntent = new Intent(Intent.ACTION_VIEW);
        String form = "";
        if(Url.substring(Url.length() -6).contains(".")) {
            form = Url.substring(Url.lastIndexOf("."));
            if(Url.startsWith("mailto"))
                form = "mailto";
            else if(Url.startsWith("http"))
                form = Url.substring(0,Url.indexOf(":"));

        } else if(!Url.startsWith("http")) {
            form = "*";
        } else if(Url.startsWith("http")){
            if(Url.contains("protonmail"))
                form = "protonmail";
            else if(Url.startsWith("http") || Url.startsWith("https"))
                form = "https";
            //form = Url.substring(0,Url.indexOf(":"));
        }

        if(form.contains(" "))
            form = form.substring(0,form.indexOf(" "));
        form = form.trim();

        switch (form) {

            case (".doc"):
            case (".docx"): case (".odt"): case (".ott"): {
                progrIntent.setDataAndType(Uri.parse(Url), "application/" +form);
                break;
            }
            case ("http"): {
                progrIntent = new Intent(Intent.ACTION_VIEW);
                progrIntent.setData(Uri.parse(Url));
                fileBrowser.startActivity(progrIntent);
                return;
            }
            case (".html"): case ("https"):{
                if(fileBrowser.haveNetwork()) {
                    fileBrowser.intendStarted = true;
                    Bundle bund = new Bundle();
                    bund.putString("URL", Url);
                    changeIcon(headMenueIcon[6], "headMenueIcons", "closed", "open");
                    fragmentStart(webBrowserDisplay, 8, "webBrowserDisplay", bund, 1, 1,
                            displayWidth - 2, displayHeight - 2);
                } else
                    fileBrowser.messageStarter("mailNoInternet", docu_Loader("Language/" + language + "/NoInternet_avaliable.txt"), 6000);

                return;
            }
            case (".txt"): case (".pdf"): {
                calledBy = "extFileCall";
                fileBrowser.intendStarted = true;
                Bundle bund = new Bundle();
                bund.putString("CALLER", "");
                bund.putString("FORMAT", form);
                if(form.equals(".txt")) {
                    bund.putStringArray("TEXT", docu_Loader(Url));
                }
                else {
                    if(fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible())
                        fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
                    bund.putStringArray("TEXT", new String[]{Url});
                }

                fileBrowser.closeListlinkedIcons(new ImageView[]{headMenueIcon[2], headMenueIcon01[1]}, new String[]{"headMenueIcons","sideLeftMenueIcons"});

                fileBrowser.changeIcon(headMenueIcon02[5], "sideRightMenueIcons", "closed","open" );
                fileBrowser.fragmentStart(createTxEditor, 7,"textEditorDisplay", bund, 1, 1,
                        displayWidth -2, displayHeight -2);
                return;
            }
            case (".ppt"): {
                progrIntent.setDataAndType(Uri.parse(Url), "application/vnd.ms-powerpoint");
                break;
            }
            case (".xls"): {
                progrIntent.setDataAndType(Uri.parse(Url), "application/vnd.ms-excel");
                break;
            }
            case (".rtf"): {
                progrIntent.setDataAndType(Uri.parse(Url), "application/rtf");
                break;
            }
            case (".flac"):
            case (".ogg"):
            case (".wav"):
            case (".mp3"): {

                if (runningMediaList != null && runningMediaList.size() > 0)
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "open", "running");
                else
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "closed", "runningOne");

                Bundle bund = new Bundle();
                bund.putString("KIND_OF_MEDIA", "AUDIO");
                bund.putString("URL", Url);
                fragmentStart(showMediaDisplay, 4,"mediaDisplay", bund, 1, 1,
                        displayWidth -2, displayHeight -2);
                fileBrowser.intendStarted = true;

                return;
            }
            case (".mkv"):
            case (".mp4"):
            case (".3gp"):
            case (".mpg"):
            case (".mpeg"):
            case (".Webm"):
            case (".webm"):
            case (".avi"):
            case (".flc"): {
                if (runningMediaList != null && runningMediaList.size() > 0)
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "open", "running");
                else
                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "closed", "runningOne");

                Bundle bund = new Bundle();
                bund.putString("KIND_OF_MEDIA", "VIDEO");
                bund.putString("URL", Uri.parse(Url).toString());
                fragmentStart(showMediaDisplay, 4,"mediaDisplay", bund, 1, 1,
                        displayWidth -2, displayHeight -2);

                fileBrowser.intendStarted = true;
                return;
            }
            case (".gif"): case (".jpg"): case (".png"): case (".jpeg"): case (".JPG"):{
                Bundle bund = new Bundle();
                bund.putString("KIND_OF_MEDIA", "PICTURES");
                bund.putString("URL", Uri.parse(Url).toString());
                fragmentStart(showMediaDisplay, 4,"mediaDisplay", bund, 1, 1,
                        displayWidth -2, displayHeight -2);

                fileBrowser.intendStarted = true;
                return;
            }
            case (".zip"): case(".tar"): {
                progrIntent.setDataAndType(Uri.parse(Url), "application/vnd.android.package-archive");
                break;
            }
            case (".apk"): {
                serveAPK(Url, "application/vnd.android.package-archive");
                return;
            }
            case ("mailto"): {

                if(fileBrowser.createSendEmail == null || !fileBrowser.createSendEmail.isVisible()) {
                    Bundle bund = new Bundle();
                    bund.putString("EMAILADD", Url.substring(Url.indexOf(":")+1).trim());

                    fragmentStart(createSendEmail, 5, "emailDisplay", bund, 1, 1,
                            displayWidth -2, displayHeight -2);
                }
                return;
            }
            case ("protonmail"): {
                Bundle bund = new Bundle();
                bund.putString("URL", Url);
                fragmentStart(webBrowserDisplay, 8, "webBrowserDisplay", bund, 1, 1,
                        displayWidth -2, displayHeight -2);
                return;
            }
            case ("*"): {
                progrIntent.setData(Uri.parse(Url));
            }
        }

        try {
            progrIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            progrIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Intent intent = Intent.createChooser(progrIntent, "Open File");
            context.startActivity(intent);

        } catch (ActivityNotFoundException ae) {
            fileBrowser.messageStarter("Message", fileBrowser.docu_Loader("Language/" + fileBrowser.language + "/No_Program_found.txt"),
                    5000);
        }
    }

    public void frameContainerMove(int frameID, FrameLayout frameId, float xpos, float ypos, int width, int height) {
        frameLy.remove(frameID);
        frameLy.add(frameID,frameId);


            fileBrowser.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    frameId.setLayoutParams(new FrameLayout.LayoutParams(width, height));
                    frameLy.get(frameID).setClickable(true);
                }
            });

        frameId.setX(xpos);
        frameId.setY(ypos);

    }

    //
    public void fragmentStart(Fragment kind_of_fragment, int a, String kind_of, Bundle transParam, int xpos, int ypos, int width, int height) {

        if (kind_of_fragment != null && kind_of_fragment.isVisible()) {
            fragmentShutdown(kind_of_fragment, a);
        }

        fragTrans = fragManager.beginTransaction();

        if (kind_of.equals("message")) {
            showMessage = showMessageFragment.newInstance();
            if (transParam != null)
                showMessage.setArguments(transParam);
            frameContainerMove(0, fileBrowser.findViewById(R.id.messageFrame), xpos, ypos, width, height);
            fragTrans.replace(R.id.messageFrame, showMessage);
        } else if (kind_of.equals("fileBrowser01")) {
            filebrowser_01 = fileBrowser_01Fragment.newInstance();
            if (transParam != null)
                filebrowser_01.setArguments(transParam);

            frameContainerMove(1, findViewById(R.id.FileBrowser_01), gridMiddleFrame.getX(), gridMiddleFrame.getY() + mainDisplayGrid.getY(),
                    gridMiddleFrame.getWidth(), gridMiddleFrame.getHeight());
            fragTrans.replace(R.id.FileBrowser_01, filebrowser_01);
        } else if (kind_of.startsWith("list")) {
            showList = showListFragment.newInstance();
            if (transParam != null)
                showList.setArguments(transParam);

            frameContainerMove(3, findViewById(R.id.listFrame), xpos, ypos, width, height);
            fragTrans.replace(R.id.listFrame, showList);

        } else if (kind_of.equals("mediaDisplay")) {
            fragId = 4;
            showMediaDisplay = MediaDisplayFragment.newInstance();
            if(transParam != null) {
                if(!transParam.toString().contains("AUDIO")) {
                    if (openFrags.equals(""))
                        openFrags = "" + fragId;
                    else
                        openFrags = openFrags + fragId;
                }
                showMediaDisplay.setArguments(transParam);
            }
            frameContainerMove(fragId, findViewById(R.id.mediaDisplay), xpos, ypos, width, height);
            fragTrans.replace(R.id.mediaDisplay, showMediaDisplay);
        } else if (kind_of.equals("emailDisplay")) {
            fragId = 5;
            if(openFrags.equals(""))
                openFrags = ""+fragId;
            else
                openFrags = openFrags +fragId;
            createSendEmail = emailDisplayFragment.newInstance();
            if (transParam != null)
                createSendEmail.setArguments(transParam);
            frameContainerMove(fragId, findViewById(R.id.createSendEmail), xpos, ypos, width, height);
            fragTrans.replace(R.id.createSendEmail, createSendEmail);
        } else if (kind_of.equals("textEditorDisplay") || kind_of.equals("pdfEditorDisplay")) {
            fragId = 7;
            if(openFrags.equals(""))
                openFrags = ""+fragId;
            else
                openFrags = openFrags +fragId;
            createTxEditor = TextEditorFragment.newInstance();
            if (transParam != null)
                createTxEditor.setArguments(transParam);
            frameContainerMove(fragId, findViewById(R.id.createTextDisplay), xpos, ypos, width, height);
            fragTrans.replace(R.id.createTextDisplay, createTxEditor);
        } else if (kind_of.equals("webBrowserDisplay")) {
            fragId = 8;
            if(openFrags.equals(""))
                openFrags = ""+fragId;
            else
                openFrags = openFrags +fragId;
            webBrowserDisplay = WebBrowserFragment.newInstance();
            if (transParam != null)
                webBrowserDisplay.setArguments(transParam);
            frameContainerMove(fragId, findViewById(R.id.createWebbrowsertDisplay), xpos, ypos, width, height);
            fragTrans.replace(R.id.createWebbrowsertDisplay, webBrowserDisplay);
        } else if (kind_of.equals("softKeyBoard")) {

            softKeyBoard = SoftKeyBoard.newInstance();
            if (transParam != null)
                softKeyBoard.setArguments(transParam);
            frameContainerMove(6, findViewById(R.id.softKeyBoard), xpos, ypos, width, height);
            fragTrans.replace(R.id.softKeyBoard, softKeyBoard);fragTrans.replace(R.id.softKeyBoard, softKeyBoard);
        }

        fragTrans.addToBackStack(""+fragId).commitAllowingStateLoss();
    }

    public void fragmentShutdown (Fragment kind_of, int n) {
        if(openFrags.contains(""+n)) {
            openFrags = openFrags.substring(0, openFrags.indexOf("" + n)) + openFrags.substring(openFrags.indexOf("" + n) + 1);
        }
        fragTrans = fragManager.beginTransaction();

        fragTrans.remove(kind_of);
        try {
            fragTrans.commit();
        } catch( Exception e) {}


            fileBrowser.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    frameLy.get(n).setLayoutParams(new FrameLayout.LayoutParams(0, 0));
                    frameLy.get(n).setClickable(false);
                    frameLy.get(n).setX(0);
                    frameLy.get(n).setY(0);
                }
            });




        if(n == 8)
            calledBack = "";
    }


    public void messageStarter(String kind_of_message, String[] message, int timer) {
        int posx,posy,width,height;
        if(yfact <= 0.625) {
            posx = displayWidth / 6;
            posy = displayHeight / 3;
            width = 2 * displayWidth / 3;

            if(kind_of_message.equals("Instruction_Manuel")) {
                posy = displayHeight / 7;
                height = 5 * displayHeight / 7;
            }
            else if(timer == 0)
                height = displayHeight / 3;
            else
                height = displayHeight /4;
        } else {
            posx = displayWidth / 4;
            posy = displayHeight / 3;
            width = 2*displayWidth / 4;
            if(kind_of_message.equals("Instruction_Manuel")) {
                posy = displayHeight / 7;
                height = 5*displayHeight / 7;
            }
            else if(timer == 0)
                height = displayHeight / 3;
            else
                height = displayHeight /5;
        }
        if(kind_of_message.equals("httpsRequest")) {

            width = 7*displayWidth/9;
            height = displayHeight/14;
            posx = displayWidth/9;
            posy = displayHeight/14;

        }
        if(kind_of_message.equals("pdfCombinedDocument_Save")) {
            height = 4*displayHeight / 7;
            posy = (int)(displayHeight/7);
        }

        Bundle bund = new Bundle();
        bund.putString("KINDOF", kind_of_message);
        bund.putStringArray("MESSAGE_STRING", message);
        bund.putInt("MESSAGE_TIMER", timer);

        fragmentStart(showMessage, 0, "message",bund,posx,posy,width,height);
    }

    public void doPrint(View view) {
        PrintHelper printHelper = new PrintHelper(this);

        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = viewToBitmap(view);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        view.draw(canvas);

        printHelper.printBitmap("Print Bitmap", bitmap);

    }
    public Bitmap viewToBitmap(View view) {

        Bitmap.Config bmpConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), bmpConfig);
        return bmp;
    }
    public void reloadFileBrowserDisplay() {
        if(urldevice != null && devicePath != null) {
            devicePath_trans = devicePath;
            String url = devicePath,
                    urldevicePath = urldevice;
            if (!url.equals(urldevice))
                urldevicePath = url.substring(urldevice.length() + 1);
            final String udPath = urldevicePath;

            paramList = fileBrowser.createArrayList(urldevicePath);

            if (udPath.length() != 0 || udPath.contains("/")) {

                    fileBrowser.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileBrowser.createFolder(urldevice);
                            fileBrowser.fragmentStart(fileBrowser.filebrowser_01, 1, "fileBrowser01", null, (int) (250 * xfact), (int) (440 * yfact),
                                    (int) (4 * displayWidth / 5 - 80 * yfact), (int) (5 * displayHeight / 7));
                        }
                    });


            } else {

                    fileBrowser.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileBrowser.createFolder(urldevice);
                        }
                    });

            }
        }
    }

    public boolean deleteDir_Files (String dirfile) {
        File[] file = new File(dirfile).listFiles();

        if(file != null && file.length > 0) {
            for (int a=0;a<file.length;a++) {

                if (file[a].isDirectory()) {
                    File[] addFile = file[a].listFiles();

                    if(addFile.length == 0) {
                        file[a].delete();
                    }

                    for (int i = 0; i < addFile.length; i++) {
                        file = Arrays.copyOf(file, file.length + 1);
                        file[file.length - 1] = addFile[i];
                    }
                } else if (file[a].isFile()) {
                    file[a].delete();
                }

            }
        }

        return true;
    }


    public void startTerminalCommands(String todo, String from, String to) {

        String f = from, t = to;

        while (from.contains("/") && f.contains(" ")) {
            String tab = f.substring(0,f.indexOf(" ")), nx = f.substring(f.indexOf(" "));
            int lst = 0, nex = f.length();
            if(tab.contains("/"))
                lst = f.indexOf(" ") -(f.indexOf(" ") -(tab.lastIndexOf("/") + 1));
            if(nx.contains("/"))
                nex = f.indexOf(" ")  + nx.indexOf("/");
            f= f.substring(0,lst)+f.substring(nex);

            from = from.substring(0,lst) +"'"+ from.substring(lst,nex).replace(" ","\\ ") +"'" + from.substring(nex);
        }

        while (to.contains("/") && t.contains(" ")) {
            String tab = t.substring(0,t.indexOf(" ")), nx = t.substring(t.indexOf(" "));
            int lst = 0, nex = t.length();
            if(tab.contains("/"))
                lst = t.indexOf(" ") -(t.indexOf(" ") -(t.lastIndexOf("/") + 1));
            if(nx.contains("/"))
                nex = t.indexOf(" ")  + nx.indexOf("/");
            t= t.substring(0,lst)+t.substring(nex);

            to = to.substring(0,lst) +"'"+ to.substring(lst,nex).replace(" ","\\ ") +"'" + to.substring(nex);
        }


        if (fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible())
            fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);
        String exe = "";
        String[] outputTx = new String[0];
        String searchWhat = "";
        Process process = null;
        try {
            if (todo.startsWith("ls")) {
                outputTx = docu_Loader("Language/" + language + "/Search_Result.txt");
                searchWhat = to.substring(to.lastIndexOf(" ") + 1);
                to = "";
            } else if (todo.contains("-rfx")) {
                String[] temp = fileBrowser.read_writeFileOnInternalStorage("read","TrashIndex","","");
                for(String s:temp) {
                    new File(getFilesDir() + "/TrashIndex", s).delete();
                }
                fileBrowser.read_writeFileOnInternalStorage("write", "pathCollection", "PathList.txt", "");
                fileBrowser.changeIcon(headMenueIcon01[headMenueIcon01.length - 1], "sideLeftMenueIcons", "open", "closed");
                return;
            }

            exe = (todo + " " + from + " " + to).trim();
            Log.e("mainExtProgram", exe);
            process = Runtime.getRuntime().exec(exe);
            process.waitFor();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String strFolder = "", strRead;

            while ((strRead = reader.readLine()) != null) {
                if (strRead.endsWith(":")) {
                    outputTx = Arrays.copyOf(outputTx, outputTx.length + 1);
                    outputTx[outputTx.length - 1] = strRead;
                    strFolder = strRead;
                } else if (strRead.contains(searchWhat)) {
                    outputTx[outputTx.length - 1] = outputTx[outputTx.length - 1] + strRead;
                    outputTx = Arrays.copyOf(outputTx, outputTx.length + 1);
                    outputTx[outputTx.length - 1] = strFolder;
                }
            }

            reader.close();

            String kind = "";
            if (todo.contains(" "))
                switch (todo.substring(0, todo.indexOf(" "))) {
                    case ("mv"): {
                        if (to.contains("TrashIndex"))
                            kind = "deletetoTrash";
                        else if (from.contains("TrashIndex")) {
                            kind = "delete "+to.substring(to.lastIndexOf("/"))+" fromTrash";
                        }
                        break;
                    }
                    case ("ls"): {
                        transList = new ArrayList<>();fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "closed");

                        String[] goout = new String[0];
                        for (int o = 0; o < outputTx.length; o++)
                            if (!outputTx[o].endsWith(":")) {
                                goout = Arrays.copyOf(goout, goout.length + 1);
                                goout[goout.length - 1] = outputTx[o];
                                transList.add(outputTx[o].replace(":", "/"));
                            }

                        fileBrowser.messageStarter("FindResult", goout, 0);

                        return;
                    }
                    case ("rm") : {
                        kind = "delete";
                        break;
                    }
                }

            String[] mess = docu_Loader("Language/" + language + "/Successful_Action.txt");
            fileBrowser.messageStarter("successAction " + kind, mess, 5000);
            fileBrowser.reloadFileBrowserDisplay();

        } catch (Exception ie) {
            String[] mess = docu_Loader("Language/" + language + "/Unsuccessful_Action.txt");
            mess = Arrays.copyOf(mess, mess.length + 1);
            mess[mess.length - 1] = ie.getMessage();
            fileBrowser.messageStarter("Instruction", mess, 5000);
            commandString = "";
        }

        //fileBrowser.reloadFileBrowserDisplay();
    }

    public void startMovePanel (int frame) {
        Thread mover = new movePanel(frame);
        mover.start();

    }


    static public class movePanel extends Thread {
        boolean go = true;
        int framely = 0;

        public movePanel(int frame) {framely = frame;}


        public void run() {
            int n = 0;
            while (go) {

                fileBrowser.frameLy.get(framely).setX(fileBrowser.frameLy.get(framely).getX() + (n * panel_direction));
                try {
                    Thread.sleep(130);
                } catch (InterruptedException ie) {
                }
                if (panel_direction == 1 && fileBrowser.frameLy.get(framely).getX() > fileBrowser.displayWidth) {
                    go = false;
                    panel_direction = -1;


                    fileBrowser.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            fileBrowser.headMenueIcon02[2].setTag(
                                    fileBrowser.headMenueIcon02[2].getTag().toString().replace("closed", "open"));
                            fileBrowser.headMenueIcon02[2].setImageBitmap(bitmapLoader("Icons/sideRightMenueIcons/" +
                                    fileBrowser.headMenueIcon02[2].getTag().toString().substring(
                                            fileBrowser.headMenueIcon02[2].getTag().toString().indexOf(" ") + 1)));
                            fileBrowser.headMenueIcon02[2].setEnabled(true);
                            switch (framely) {
                                case (4) : {
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "open", "running");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "open");

                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "running", "open");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "running", "open");
                                    fileBrowser.showMediaDisplay.switcher.setImageBitmap(fileBrowser.bitmapLoader("Icons/" + "switcher_closed.png"));

                                    fileBrowser.fragId = 4;
                                    break;
                                }
                                case (5) : {
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "open", "running");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "running", "open");

                                    if(fileBrowser.showMediaDisplay != null && (fileBrowser.headMenueIcon02[3].getTag().toString().contains("runningOne") &&
                                            fileBrowser.showMediaDisplay.kindOfMedia.equals("VIDEO")) )
                                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "open");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "open");
                                    fileBrowser.createSendEmail.switcher.setImageBitmap(fileBrowser.bitmapLoader("Icons/" + "switcher_closed.png"));

                                    fileBrowser.fragId = 5;
                                    break;
                                }
                                case (7) : {
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "open", "running");
                                    if(fileBrowser.showMediaDisplay != null && !fileBrowser.showMediaDisplay.kindOfMedia.equals("AUDIO"))
                                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "open");

                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "running", "open");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "running", "open");
                                    fileBrowser.createTxEditor.switcher.setImageBitmap(fileBrowser.bitmapLoader("Icons/" + "switcher_closed.png"));

                                    fileBrowser.fragId = 7;
                                    break;
                                }
                                case (8) : {
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "open", "running");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "running", "open");

                                    if(fileBrowser.showMediaDisplay != null && (fileBrowser.headMenueIcon02[3].getTag().toString().contains("runningOne") &&
                                            fileBrowser.showMediaDisplay.kindOfMedia.equals("VIDEO")))
                                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "open");
                                    fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "open");
                                    fileBrowser.webBrowserDisplay.switcher.setImageBitmap(fileBrowser.bitmapLoader("Icons/" + "switcher_closed.png"));

                                    fileBrowser.fragId = 8;
                                    break;
                                }
                            }
                        }
                    });

                } else if (panel_direction == -1 && fileBrowser.frameLy.get(framely).getX() <= 1) {
                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                        fileBrowser.fragmentShutdown(fileBrowser.showList,3);

                    if(framely == 5) {
                        if (fileBrowser.createSendEmail.attachedList != null && calledBy.equals("Attached")){

                            fileBrowser.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fileBrowser.createSendEmail.createMail = true;
                                    fileBrowser.createSendEmail.attachment = false;
                                    fileBrowser.createSendEmail.createNewDisplay();
                                }
                            });
                        }
                        calledBy = "";
                    } else
                    if(framely == 7) {
                        fileBrowser.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(fileBrowser.createTxEditor.kindOfFormat.equals(".txt")) {
                                    if (fileBrowser.createTxEditor.action.equals("saveDocument")) {
                                        fileBrowser.changeIcon(fileBrowser.createTxEditor.icons[3], "TextEditorIcons", "open", "closed");
                                    }
                                }
                            }
                        });
                    }

                    go = false;
                    panel_direction = 1;
                    break;
                }
                n = n + 10;
            }

        }

    }

}
