package easysoft.freebrowser;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static android.view.MotionEvent.ACTION_UP;
import static easysoft.freebrowser.FileBrowser.*;
import static easysoft.freebrowser.showListFragment.selectedTx_01;

public class TextEditorFragment extends Fragment {
    Context context;
    View view;

    ImageView timeImage;
    AnimationDrawable timerAnimation;
    EditText[] txarea = new EditText[0];
    FrameLayout txEditorLayout, timerGifLay;
    ImageView selector, timer, switcher;
    RelativeLayout mainRel, textRel;
    LinearLayout headIconLin, iconLin, mainLin, pdfDisplayLin;
    RelativeLayout pdfDisplayRel;
    LinearLayout scaleLin;
    ScrollView pdfScroll, scView;
    int pageNr = 0, selectedTx = 0, selectedTab = -1, selectedPdfTab = 0, AnzOpenPdf = -1;
    int[] scY = new int[] {0,0,0,0,0,0,0,0,0,0};
    HorizontalScrollView headTxScroll, mainSc;
    ArrayList<String> preFixed = new ArrayList<>();
    ArrayList<RelativeLayout[]> addLayers = new ArrayList<>();

    enterOpenPdf startOpenPdf;
    Bitmap pdfOpenBit;

    static EditText TxEditor;
    TextView txDate, txSenderAddress;
    TextView[] txPages, tabTx;
    ImageView[] icons,tabImgs, importImgView = new ImageView[0];
    ImageView  imgView, activImgView;

    LinearLayout TextLin;
    String caller = "", call = "", headerTx = "", kindOfFormat = "", action = "", memoryAction = "", mainTx = "";
    String[] readedText, accountAddrData;
    boolean newPDF = false, newTX = false;
    static double scaleFact = 1;
    static String logoPath = "", memoryTx = "", loadedFile = "", isBackgroundPath = "";
    static boolean importImg = false, noAddr = true, isBackground = false, isLogo = false;

    public TextEditorFragment() {
    }

    public static TextEditorFragment newInstance() {
        TextEditorFragment fragment = new TextEditorFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            caller = getArguments().getString("CALLER");
            kindOfFormat = getArguments().getString("FORMAT");
            readedText = getArguments().getStringArray("TEXT");
        }

        accountAddrData = fileBrowser.read_writeFileOnInternalStorage("read", "accountAddrData","accountAddrData.txt", "");
        if (accountAddrData == null || accountAddrData.length == 0) {
            calledBy  = "";
            accountAddrData = fileBrowser.docu_Loader("Language/" + fileBrowser.language + "/EditorAccountData.txt");
        }
        txEditorLayout = fileBrowser.frameLy.get(7);
        noAddr = true;
        isBackground = false;
        loadedFile = devicePath;
        tabImgs = new ImageView[0];
        tabTx = new TextView[0];

        variablenInstantion(caller,kindOfFormat,readedText);
    }

    public void variablenInstantion (String call, String koF,String[] rTx) {
        context = getContext();

        caller = call;
        action = "";
        kindOfFormat = koF;
        noAddr = true;
        isLogo = false;
        logoPath = "";
        readedText = rTx;
        mainTx = "";
        isBackground = false;
        loadedFile = devicePath;
        pageNr = 0;
        pdfPageCount = 0;

        selectedTab = preFixed.size();

        if(kindOfFormat.equals(".txt")) {
            mainLin = new LinearLayout(context);
            mainLin.setOrientation(LinearLayout.VERTICAL);
            mainLin.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, displayHeight - displayHeight / 12));

            mainLin.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        }

        if(kindOfFormat.equals(".txt") && !caller.endsWith("New")) {
            newTX = false;
            for (String s : readedText) {
                if (s.startsWith("An: ") || s.startsWith("To: ")) {
                    headerTx = mainTx;
                    mainTx = "";
                }
                mainTx = mainTx + s + "\n";
            }
        } else if(kindOfFormat.equals(".pdf") && !caller.endsWith("New")) {
            newPDF = false;
            scaleFact = 1.0;
            selectedTx_01 = 0;
            AnzOpenPdf++;
            addLayers.add(new RelativeLayout[0]);
        }
        else if(kindOfFormat.equals(".txt") && caller.endsWith("New")) {
            newTX = true;
            loadedFile = devicePath + "/New_Txt.txt";
        }
        else if(kindOfFormat.equals(".pdf") && caller.endsWith("New")) {
            newPDF = true;
            selectedPdfTab = 0;
            pdfPageCount = 0;
            loadedFile = devicePath + "/New_Pdf.txt";
            AnzOpenPdf++;
            addLayers.add(new RelativeLayout[0]);
            //addLayers.set(selectedPdfTab,new RelativeLayout[1]);
        }


        preFixed.add("selectedTab: "+selectedTab+"→caller: "+ caller +"→action: "+action+" →kindOfFormat: "+kindOfFormat+"→noAddr: " +noAddr+ "→isLogo: " +isLogo+ "→logoPath: " +logoPath+
                "→isBackground: " +isBackground+ "→mainTx: " +mainTx+ "→selectedNr: "+pageNr+"→selectedPdfTab: "+AnzOpenPdf+"→pdfPageCount: "+pdfPageCount+"→devicePath: " + loadedFile);

        if(preFixed.size() > 1) {
            selectedTx = 0;
            mainRel.removeAllViews();
            mainRel.addView(createHaderIcons());

            if(kindOfFormat.equals(".txt")) {
                mainRel.addView(createTextEditorDisplay());

            } else if(kindOfFormat.equals(".pdf")) {
                mainRel.addView(createPdfEditorDisplay());
            }
            mainRel.addView(createTabIcons());
            mainRel.addView(timeImage);
            mainRel.addView(createSwitcher());
            //fileBrowser.startMovePanel(fileBrowser.fragId);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_text_editor, container, false);
        mainRel = (RelativeLayout) view.findViewById(R.id.textEditorMainRel);
        mainRel.setBackgroundColor(getResources().getColor(R.color.white));
        mainRel.setPadding(5,5,5,5);
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
                fileBrowser.createTxEditor.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.createTxEditor.timerAnimation.stop();
            }
        });

        timerAnimation = (AnimationDrawable) timeImage.getBackground();
        //

        mainRel.addView(createHaderIcons());

        if(kindOfFormat.equals(".txt")) {
            mainRel.addView(createTextEditorDisplay());
        } else if(kindOfFormat.equals(".pdf")) {
            mainRel.addView(createPdfEditorDisplay());
        }
        mainRel.addView(createTabIcons());
        mainRel.addView(timeImage);
        mainRel.addView(createSwitcher());
        txEditorLayout.bringToFront();
        return view;
    }

    private ImageView createSwitcher() {
        switcher = new ImageView(context);
        switcher.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth / 15, displayHeight / 2));
        switcher.setImageBitmap(fileBrowser.bitmapLoader("Icons/" + "switcher_closed.png"));
        switcher.setX(displayWidth - displayWidth / 13);
        switcher.setY(displayHeight / 12);

        switcher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ((ImageView) view).setImageBitmap(fileBrowser.bitmapLoader("Icons/" + "switcher_open.png"));
                if(kindOfFormat.equals(".txt"))
                    mainTx = TxEditor.getText().toString();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                }

                if (fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                    calledBy = "textEditorBack";
                    if (kindOfFormat.equals(".txt"))
                        refreshToFillIn();
                    fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                    fileBrowser.closeListlinkedIcons(new ImageView[]{headMenueIcon01[1], fileBrowser.createTxEditor.icons[1], fileBrowser.createTxEditor.icons[2], fileBrowser.createTxEditor.icons[3]},
                            new String[]{"sideLeftMenueIcons", "TextEditorIcons", "TextEditorIcons", "TextEditorIcons"});
                }

                if (loadedFile != null && loadedFile.length() > 0)
                    fileBrowser.reloadFileBrowserDisplay();
                for(int i=1;i<icons.length;i++)
                    fileBrowser.changeIcon(icons[i],"TextEditorIcons","open","closed");
                fileBrowser.createTxEditor.timeImage.setVisibility(View.INVISIBLE);
                fileBrowser.createTxEditor.timerAnimation.stop();

                fileBrowser.startMovePanel(7);

                if (fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible()) {
                    fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
                }
                return true;
            }
        });

        return switcher;
    }

    public void refreshToFillIn() {

        String fix = preFixed.get(selectedTab);
           String[] fixs = fix.split("→");
           fix = "";
           for (String s : fixs) {
               switch(s.substring(0,s.indexOf(": ")+2)) {
                   case("selectedTab: "): {
                       s = s.substring(0,s.indexOf(": ")+2) + selectedTab;
                       break;
                   }
                   case("caller: "): {
                       s = s.substring(0,s.indexOf(": ")+2) + caller;
                       break;
                   }
                   case("action: "): {
                       s = s.substring(0,s.indexOf(": ")+2) + action;
                       break;
                   }
                   case("noAddr: "): {
                       if(kindOfFormat.equals(".txt"))
                          s = s.substring(0,s.indexOf(": ")+2) + noAddr;
                       break;
                   }
                   case("isLogo: "): {
                       if(kindOfFormat.equals(".txt"))
                          s = s.substring(0,s.indexOf(": ")+2) + isLogo;
                       break;
                   }
                   case("logoPath: "): {
                       if(kindOfFormat.equals(".txt"))
                          s = s.substring(0,s.indexOf(": ")+2) + logoPath;
                       break;
                   }
                   case("isBackground: "): {
                       if(kindOfFormat.equals(".txt"))
                          s = s.substring(0,s.indexOf(": ")+2) + isBackground;
                       break;
                   }
                   case("mainTx: "): {
                       if(kindOfFormat.equals(".txt"))
                          s = s.substring(0,s.indexOf(": ")+2) + mainTx;
                       break;
                   }
                   case("selectedNr: "): {
                       if(kindOfFormat.equals(".pdf"))
                          s = s.substring(0,s.indexOf(": ")+2) + pageNr;
                       break;
                   }
                   case("pdfPageCount: "): {
                       if(kindOfFormat.equals(".pdf"))
                          s = s.substring(0,s.indexOf(": ")+2) + pdfPageCount;
                       break;
                   }
               }
               fix = fix + s +"→";
           }
           fix = fix.substring(0,fix.lastIndexOf("→"));
           preFixed.set(selectedTab,fix);
    }
    public void refreshToDefine() {
        String fix = preFixed.get(selectedTab);
        String[] fixs = fix.split("→");
        selectedTab = 0;
        caller = "";
        action = "";
        kindOfFormat = "";
        noAddr = false;
        isLogo = false;
        logoPath = "";
        isBackground = false;
        mainTx = "";
        pageNr = 0;
        selectedPdfTab = 0;
        pdfPageCount =0;
        loadedFile = "";
        for (String s : fixs) {
            switch(s.substring(0,s.indexOf(": ")+2)) {
                case("selectedTab: "): {
                    selectedTab = Integer.parseInt(s.substring(s.indexOf(": ")+2));
                    break;
                }
                case("caller: "): {
                    caller = s.substring(s.indexOf(": ")+2);
                    break;
                }
                case("action: "): {
                    action = s.substring(s.indexOf(": ")+2);
                    break;
                }
                case("kindOfFormat: "): {
                    kindOfFormat = s.substring(s.indexOf(": ")+2);
                    break;
                }
                case("noAddr: "): {
                    if(kindOfFormat.equals(".txt"))
                       noAddr = Boolean.parseBoolean(s.substring(s.indexOf(": ")+2));
                    break;
                }
                case("isLogo: "): {
                    if(kindOfFormat.equals(".txt"))
                       isLogo = Boolean.parseBoolean(s.substring(s.indexOf(": ")+2));
                    break;
                }
                case("logoPath: "): {
                    if(kindOfFormat.equals(".txt"))
                       logoPath = s.substring(s.indexOf(": ")+2);
                    break;
                }
                case("isBackground: "): {
                    if(kindOfFormat.equals(".txt"))
                       isBackground = Boolean.parseBoolean(s.substring(s.indexOf(": ")+2));
                    break;
                }
                case("mainTx: "): {
                    if(kindOfFormat.equals(".txt"))
                       mainTx = s.substring(s.indexOf(": ")+2);
                    break;
                }
                case("selectedNr: "): {
                    if(kindOfFormat.equals(".pdf"))
                       pageNr = Integer.parseInt(s.substring(s.indexOf(": ")+2));
                    break;
                }
                case("selectedPdfTab: "): {
                    if(kindOfFormat.equals(".pdf"))
                       selectedPdfTab = Integer.parseInt(s.substring(s.indexOf(": ")+2));
                    break;
                }
                case("pdfPageCount: "): {
                    if(kindOfFormat.equals(".pdf"))
                       pdfPageCount = Integer.parseInt(s.substring(s.indexOf(": ")+2));
                    break;
                }
                case("devicePath: "): {
                     loadedFile = s.substring(s.indexOf(": ")+2);
                    break;
                }
            }
        }
    }

    public LinearLayout createTextEditorDisplay() {
        float f = 12, f1 = 28;
        if(yfact <= 0.625) {
            f = 18;
            f1 = 32;
        }

        if(textRel != null)
            mainLin.removeView(textRel);


        textRel = new RelativeLayout(context);
        textRel.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        //textRel.setBackgroundColor(getResources().getColor(R.color.black_overlay));
        textRel.setX(displayWidth/f1);
        textRel.setY(displayHeight/f);

        LinearLayout scaleLin = new LinearLayout(context);
        scaleLin.setLayoutParams(new RelativeLayout.LayoutParams(4*displayHeight/12, displayHeight/12));
        scaleLin.setOrientation(LinearLayout.HORIZONTAL);
        scaleLin.setPadding(10,10,10,10);
        scaleLin.setX(displayWidth -4*displayHeight/8);
        scaleLin.setY(displayHeight -3*displayHeight/12);

        String[] scaleTx = new String[]{"minus", "lupe", "plus"};
        ImageView[] scaleImg = new ImageView[scaleTx.length];
        for(int i=0;i<scaleTx.length;i++) {
            scaleImg[i] = new ImageView(context);
            scaleImg[i].setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/"+scaleTx[i]+".png"));
            scaleLin.addView(scaleImg[i]);
        }

        TextLin = new LinearLayout(context);
        TextLin.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,  RelativeLayout.LayoutParams.WRAP_CONTENT));
        TextLin.setPadding(txEditorLayout.getWidth()/16,txEditorLayout.getHeight()/28, txEditorLayout.getWidth()/18,txEditorLayout.getHeight()/16);
        TextLin.setOrientation(LinearLayout.HORIZONTAL);
        //TextLin.setBackgroundColor(getResources().getColor(R.color.black_overlay));

        if(isBackground) {
            mainTx = "";

            for(int i=0;i<65;i++)
                mainTx = mainTx +"\n";

            if(isBackgroundPath.endsWith(".pdf")) {

                Bitmap bitmap;
                openPdfStart(0,new File(isBackgroundPath));
                try {
                    bitmap = pdfOpenBit; //openPdf(0, new File(isBackgroundPath));
                    isBackgroundPath = isBackgroundPath.replace("pdf","png");
                    OutputStream output = new FileOutputStream(new File(isBackgroundPath));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 1, output);
                } catch(IOException io) {Log.e("output", io.getMessage());}

                this.mainLin.removeView(pdfDisplayLin);
            }

            Drawable draw = Drawable.createFromPath(isBackgroundPath);
            textRel.setBackground(draw);
        }

        headIconLin = new LinearLayout(context);
        headIconLin.setPadding(40,20,20,40);
        RelativeLayout.LayoutParams headiconRelParam = new RelativeLayout.LayoutParams(displayWidth, displayHeight/9);
        headiconRelParam.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout headiconRel = new RelativeLayout(context);
        headiconRel.setLayoutParams(headiconRelParam);

        ScrollView txScroll = new ScrollView(context);
        txScroll.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        if((action.length() > 0 && action.contains("Address")) && !(mainTx.contains("(!") && mainTx.contains("!)"))) {
            noAddr = false;
            calledBy = "textEditor";

            if(logoPath.endsWith(".png")&& action.contains("Logo")) {
                ImageView headIconView = new ImageView(context);
                headIconView.setLayoutParams(headiconRelParam);
                headIconView.setImageBitmap(fileBrowser.bitmapLoader(logoPath));
                headIconView.setX(txEditorLayout.getWidth()/5);
                headiconRel.addView(headIconView);

            }

            if(accountAddrData != null && accountAddrData.length > 0) {
                StringBuffer trans = new StringBuffer();
                for (int i=4; i< accountAddrData.length; i++)
                    trans.append(accountAddrData[i].substring(accountAddrData[i].indexOf(":") +1).trim().replace(
                            "(!", "").replace("!)", "") +"\n");
                txSenderAddress = new TextView(context);
                txSenderAddress.setTextSize(textSize -1);
                if(yfact<0.62)
                    txSenderAddress.setTextSize(textSize -2);
                txSenderAddress.setText(trans.toString());
                txSenderAddress.setTextColor(getResources().getColor(R.color.black));
                txSenderAddress.setX(displayWidth/3);
                txSenderAddress.setY(displayHeight / 26);

                headiconRel.addView(txSenderAddress);

                txDate = new TextView(context);
                txDate.setTextSize(textSize);
                txDate.setText(new SimpleDateFormat("dd.MM.y").format(new Date()));
                txDate.setTextColor(getResources().getColor(R.color.black));
                txDate.setX(10);
                txDate.setY(displayHeight / 24);

                headiconRel.addView(txDate);

            }
            if (mainTx.length() <= 2) {
                String[] headLines = fileBrowser.docu_Loader("Language/" + fileBrowser.language + "/TextHeadLines.txt");
                headerTx = "";
                for (String s : headLines) {
                    headerTx = headerTx + s +"\n\n";
                }
                mainTx = headerTx + mainTx;
            }

            headIconLin.addView(headiconRel);
            headIconLin.setY(displayHeight/22);
            textRel.addView(headIconLin);
            TextLin.setY(headiconRelParam.height);

        } /*else {
            TextLin.setY(displayHeight/22);
            mainTx = headerTx + mainTx;
            headerTx = "";
        }*/

        TxEditor = new EditText(context);
        TxEditor.setTextColor(getResources().getColor(R.color.black));
        if(isBackground)
            TxEditor.setTextColor(getResources().getColor(R.color.white));
        TxEditor.setText(mainTx);
        TxEditor.setTextSize(textSize);
        TxEditor.setPadding(10,10,10,10);
        TxEditor.setGravity(Gravity.TOP);
        TxEditor.setShowSoftInputOnFocus(false);
        TxEditor.setDrawingCacheEnabled(true);

        if(caller.endsWith("New")) {
            if(mainTx.length() == 0)
                TxEditor.setText(" \n");
            if(TxEditor.getText().toString().length() < 60)
               TxEditor.setSelection(TxEditor.getText().toString().indexOf(":")+2);
            else
                TxEditor.setSelection(TxEditor.getText().toString().length() -1);
            TxEditor.requestFocus();
            int fact = displayHeight/18,
                    fact01 = displayHeight/18;
            if(yfact < 0.625) {
                fact = displayHeight / 28;
                fact01 = 0;
            }
            if(yfact >= 0.8) {
                fact01 = displayHeight/12;
            }

            if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible())
                fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6,"softKeyBoard",null,5,(int)(2*displayHeight/3 -fact),
                        displayWidth -10, (int)(displayHeight/3) +fact01);
        }
        TxEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible()) {
                    int fact = displayHeight / 18,
                            fact01 = displayHeight / 18;
                    if (yfact < 0.625) {
                        fact = displayHeight / 28;
                        fact01 = 0;
                    }
                    if (yfact >= 0.8) {
                        fact01 = displayHeight / 12;
                    }
                    fileBrowser.keyboardTrans = ((EditText) v);

                    fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6, "softKeyBoard", null, 5, (int) (2 * displayHeight / 3 - fact),
                            displayWidth - 10, (int) (displayHeight / 3) + fact01);
                }
            }
        });

        if (TxEditor.getText().toString().contains("(!") && TxEditor.getText().toString().contains("!)")) {

            selector = new ImageView(context);
            selector.setLayoutParams(new RelativeLayout.LayoutParams((int) (80 * xfact), (int) (80 * xfact)));
            selector.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/txEditorSelector.png"));
            selector.setX(3 * displayWidth / 5);
            selector.setY(displayHeight / 10);

            selector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TxEditor.getText().toString().contains("(!") && TxEditor.getText().toString().contains("!)")) {
                        TxEditor.setSelection(TxEditor.getText().toString().indexOf("(!"),
                                TxEditor.getText().toString().indexOf("!)") + 2);
                    }
                }
            });

            mainRel.addView(selector);
        }
        refreshToFillIn();
        txScroll.addView(TxEditor);
        TextLin.addView(txScroll);
        textRel.addView(TextLin);
        mainLin.addView(textRel);

        calledBy = "";
        return mainLin;
    }

    public HorizontalScrollView createTabIcons () {
        float f = 14, f1 = 20;
        if(yfact <= 0.625) {
            f = 18;
            f1 = 24;
        }
        mainRel.removeView(mainSc);

        mainSc = new HorizontalScrollView(context);
        mainSc.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth -displayWidth/8, (int)(displayHeight / f1 )));
        mainSc.setY(displayHeight/f);
        mainSc.setX(displayWidth/16);

        RelativeLayout TabRel = new RelativeLayout(context);
        TabRel.setLayoutParams(new RelativeLayout.LayoutParams(preFixed.size()*(displayWidth / 6) +10, RelativeLayout.LayoutParams.MATCH_PARENT ));
        TabRel.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        TabRel.setPadding(5, 5, 5, 5);


        tabImgs = Arrays.copyOf(tabImgs,tabImgs.length +1);
        tabTx = Arrays.copyOf(tabTx,tabTx.length +1);
        String Tx = "";

        for (int i = 0; i < preFixed.size(); i++) {

            tabImgs[i] = new ImageView(context);
            tabImgs[i].setBackgroundColor(getResources().getColor(R.color.white_overlay));
            tabImgs[i].setLayoutParams(new RelativeLayout.LayoutParams(displayWidth / 6, RelativeLayout.LayoutParams.MATCH_PARENT));
            tabImgs[i].setScaleType(ImageView.ScaleType.FIT_XY);
            tabImgs[i].setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/tab_closed.png"));
            if (i == selectedTab)
                tabImgs[i].setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/tab_open.png"));
            tabImgs[i].setX(i * displayWidth / 6);

            TabRel.addView(tabImgs[i]);

            Tx = preFixed.get(i).substring(preFixed.get(i).indexOf("devicePath: ")+12);
            Tx = Tx.substring(Tx.lastIndexOf("/")+1);

            if (Tx.length() >= 8)
                Tx = Tx.substring(0, 8) + "\n" + Tx.substring(Tx.lastIndexOf(".") + 1).toUpperCase();

            tabTx[i] = new TextView(context);
            tabTx[i].setBackgroundColor(getResources().getColor(R.color.white_overlay));
            tabTx[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tabTx[i].setLayoutParams(new RelativeLayout.LayoutParams(displayWidth / 6, RelativeLayout.LayoutParams.WRAP_CONTENT));
            tabTx[i].setPadding(20, 25, 20, 5);
            tabTx[i].setTextColor(getResources().getColor(R.color.black));
            if (i == selectedTab)
                tabTx[i].setTextColor(getResources().getColor(R.color.dark_green));

            tabTx[i].setText(Tx);
            tabTx[i].setTextSize(textSize - 2);
            tabTx[i].setX(i * displayWidth / 6 + 20);
            tabTx[i].setTag(""+i);

            tabTx[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible())
                        fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
                    if (kindOfFormat.equals(".txt") && TxEditor.getText().toString().length() > 5) {
                        mainTx = TxEditor.getText().toString();
                    }

                    refreshToFillIn();
                    int tag = Integer.parseInt(v.getTag().toString());

                    selectedTab = tag;

                    for (int i = 0; i < tabTx.length; i++) {
                        tabTx[i].setTextColor(getResources().getColor(R.color.black));
                        tabImgs[i].setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/tab_closed.png"));
                    }
                    ((TextView) v).setTextColor(getResources().getColor(R.color.dark_green));
                    tabImgs[tag].setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/tab_open.png"));
                    refreshToDefine();

                    mainRel.removeView(headTxScroll);
                    if (kindOfFormat.equals(".txt")) {
                        mainRel.removeView(mainLin);
                        mainRel.removeView(pdfDisplayLin);
                        mainRel.removeView(scView);
                        mainRel.addView(createTextEditorDisplay(), 0);
                    } else if (kindOfFormat.equals(".pdf")) {
                        mainRel.addView(createPdfEditorDisplay(), 0);
                    }
                    mainRel.addView(createHaderIcons(),0);
                }

            });
            TabRel.addView(tabTx[i]);
        }

        mainSc.addView(TabRel);
        return mainSc;

    }

    public HorizontalScrollView createHaderIcons () {

        icons = new ImageView[0];

        headTxScroll = new HorizontalScrollView(context);
        headTxScroll.setHorizontalScrollBarEnabled(false);
        headTxScroll.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth -10, RelativeLayout.LayoutParams.WRAP_CONTENT));
        headTxScroll.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        headTxScroll.setY(5);
        headTxScroll.setX(displayWidth/16);
        headTxScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                    fileBrowser.fragmentShutdown(fileBrowser.showList,3);
            }
        });
        iconLin = new LinearLayout(context);
        iconLin.setLayoutParams(new RelativeLayout.LayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)));
        iconLin.setOrientation(LinearLayout.HORIZONTAL);
        iconLin.setPadding(10,10,10,10);

        int o = 0;
        String[] list = fileBrowser.file_icon_Loader("Icons/TextEditorIcons");
        for (String s : list) {
            if (s.contains("closed")) {

                icons = Arrays.copyOf(icons, icons.length + 1);
                icons[icons.length - 1] = new ImageView(context);
                icons[icons.length - 1].setLayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth / 16 * xfact), (int) (displayWidth / 16 * xfact)));

                icons[icons.length - 1].setTag("true " + s);
                icons[icons.length - 1].setEnabled(true);
                icons[icons.length - 1].setPadding(10,0,10,0);


                if((caller.contains("EditorDisplayNew") && s.contains("document-new")) ||
                        (logoPath.endsWith(".png") && s.contains("text"))) {
                    s = s.replace("closed", "open");
                    icons[icons.length - 1].setTag("true " + s);
                }
                if((kindOfFormat.equals(".pdf") && s.contains("Info")) || (kindOfFormat.equals(".txt") && s.contains("pdf")) ||
                        ((kindOfFormat.equals(".pdf") && !caller.equals("pdfEditorDisplayNew")) && !(s.contains("pdf") || s.contains("document-new") || s.contains("Drucker")|| s.contains("save"))) ||
                        (caller.equals("pdfEditorDisplayNew") && !(s.contains("pdf") || s.contains("Drucker")|| s.contains("save"))) || (isBackground && !s.contains("Drucker"))) {
                    icons[icons.length - 1].setEnabled(false);
                    icons[icons.length - 1].setTag(icons[icons.length - 1].getTag().toString().replace("open","closed").
                            replace("true","false"));

                }


                icons[icons.length - 1].setImageBitmap(fileBrowser.bitmapLoader("Icons/TextEditorIcons/" + s));
                if (!s.contains("Empty"))
                    icons[icons.length - 1].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            calledBy = "TextEditorIcons";
                            String tag = v.getTag().toString();
                            if (tag.contains("closed")) {
                                if(!tag.contains("Drucker"))
                                   fileBrowser.changeIcon(v,"TextEditorIcons","closed","open");

                                if (tag.contains("Info")) {
                                    memoryAction = action;
                                    action = "info";
                                    calledBack = "InfoView";
                                    memoryTx = TxEditor.getText().toString();
                                    textRel.removeView(headIconLin);
                                    fileBrowser.closeListlinkedIcons(new ImageView[] {icons[1], icons[3], icons[4]}, new String[]{
                                            "TextEditorIcons", "TextEditorIcons", "TextEditorIcons"});

                                    if (accountAddrData[4].contains("(!") || accountAddrData[5].contains("(!")) {
                                        StringBuffer trans = new StringBuffer();
                                        for (int i=0; i< accountAddrData.length; i++) {
                                            if(i == accountAddrData.length -1)
                                                trans.append(accountAddrData[i]);
                                            else
                                                trans.append(accountAddrData[i] + "\n");
                                        }

                                        mainTx = trans.toString();
                                        mainLin.removeView(textRel);

                                        fileBrowser.messageStarter("Instruction_EditorAccount", docu_Loader("Language/" + language + "/Instruction_EditorAccount.txt"), 5000);
                                    } else {
                                        String trans = "";
                                        for (int i=0; i< accountAddrData.length; i++) {
                                            if (i == 3 && logoPath.length() > 0) {
                                                trans = trans + accountAddrData[i]   + logoPath.substring(logoPath.lastIndexOf("/") + 1) + "\n";
                                            } else
                                                trans = trans +accountAddrData[i] + "\n";
                                        }
                                        TxEditor.setText(trans);
                                    }

                                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);

                                } else if (tag.contains("text")) {
                                    action = "text";
                                    fileBrowser.closeListlinkedIcons(new ImageView[] {icons[3], icons[4], icons[5]}, new String[]{
                                            "TextEditorIcons", "TextEditorIcons", "TextEditorIcons"});

                                    float f = 3;
                                    if(yfact <= 0.625)
                                        f = 2;

                                    int[] iconpos = new int[2];
                                    v.getLocationOnScreen(iconpos);

                                    fileBrowser.createList("TextList",1, "Language/" + fileBrowser.language + "/NewTxtFile.txt",6,
                                            iconpos[0] +v.getWidth() +10, iconpos[1] + v.getHeight()/2, (int)(displayWidth / f), "ru");

                                    fileBrowser.frameLy.get(3).bringToFront();
                                } else if(tag.contains("pdf")) {
                                    fileBrowser.closeListlinkedIcons(new ImageView[] {icons[3], icons[1], icons[4], icons[5]}, new String[]{
                                            "TextEditorIcons", "TextEditorIcons", "TextEditorIcons", "TextEditorIcons"});
                                    action = "pdfPage";
                                    String kind = "PdfSideList";
                                    if (caller.endsWith("New"))
                                        kind = "PdfCombineList";


                                    arrayList = new ArrayList<>();

                                    if (!caller.endsWith("New") && fileBrowser.pdfPageCount > 0) {
                                        fileBrowser.changeIcon(v,"TextEditorIcons","open","closed");
                                    }

                                    if (caller.endsWith("New")) {

                                        String[] st = fileBrowser.docu_Loader("Language/" + fileBrowser.language + "/NewPdfFile.txt");
                                        for (int s = 0; s < st.length; s++)
                                            arrayList.add(new String[]{st[s]});

                                    }


                                    float f = 4;
                                    if(yfact <= 0.625)
                                        f = 3;

                                    int[] iconpos = new int[2];
                                    v.getLocationOnScreen(iconpos);

                                    fileBrowser.createList(kind,1, "",6,
                                            iconpos[0] +v.getWidth() +10, iconpos[1] + v.getHeight()/2, (int)(displayWidth / f), "ru");

                                    fileBrowser.frameLy.get(3).bringToFront();
                                } else if(tag.contains("save")) {
                                    if(loadedFile.length() > 0 && loadedFile.contains("/")) {

                                        fileBrowser.closeListlinkedIcons(new ImageView[]{icons[2], icons[1], icons[4], icons[5]}, new String[]{
                                                "TextEditorIcons", "TextEditorIcons", "TextEditorIcons", "TextEditorIcons"});
                                        action = "saveDocument";

                                        if (kindOfFormat.equals(".pdf")) {
                                            fileBrowser.isPdf = true;
                                            if (calledBy.equals("importImg"))
                                                calledBy = "";
                                        } else
                                            fileBrowser.isPdf = false;

                                        float f = 4;
                                        if (yfact <= 0.625)
                                            f = 3;

                                        int[] iconpos = new int[2];
                                        v.getLocationOnScreen(iconpos);

                                        fileBrowser.createList("PdfSaveList", 1, "Language/" + fileBrowser.language + "/TxEditorSave.txt", 6,
                                                iconpos[0] + v.getWidth() + 10, iconpos[1] + v.getHeight() / 2, (int) (displayWidth / f), "ru");

                                        fileBrowser.frameLy.get(3).bringToFront();
                                    } else {
                                        fileBrowser.createTxEditor.timeImage.setVisibility(View.INVISIBLE);
                                        fileBrowser.createTxEditor.timerAnimation.stop();

                                        fileBrowser.messageStarter("Unsuccessful_TxDocumentSave", docu_Loader("Language/" + language + "/Unsuccsessful_TxDocumentSave.txt"), 5000);
                                        fileBrowser.changeIcon(v,"TextEditorIcons","open","closed");

                                    }
                                } else if (tag.contains("document")) {
                                    action = "createDocument";
                                    if(kindOfFormat.equals(".pdf")) {
                                        caller = "pdfEditorDisplayNew";
                                        arrayList = new ArrayList<>();
                                        fileBrowser.changeIcon(icons[2],"TextEditorIcons","open","closed");
                                        if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                                            fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                    } else
                                        fileBrowser.changeIcon(v,"TextEditorIcons","open","closed");
                                } else if (tag.contains("Drucker")) {
                                    if(kindOfFormat.equals(".txt"))
                                        fileBrowser.doPrint(textRel);
                                    else if(kindOfFormat.equals(".pdf"))
                                        fileBrowser.doPrint(pdfDisplayLin);
                                }
                            }
                            else if (tag.contains("open")) {

                                fileBrowser.changeIcon(v,"TextEditorIcons","open","closed");
                                if (tag.contains("Info")) {
                                    saveInfo();
                                } else if (tag.contains("text")) {
                                    if(logoPath.endsWith(".png"))
                                        logoPath = logoPath.substring(0, logoPath.lastIndexOf("."));
                                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);

                                } else if (tag.contains("document")) {
                                    if(kindOfFormat.equals(".pdf")) {
                                        caller = "pdfEditorDisplay";
                                        fileBrowser.isPdf = false;

                                        if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                                            fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                        //iconLin.addView(createScaleButtons());
                                    } else
                                        fileBrowser.changeIcon(v,"TextEditorIcons","closed","open");
                                } else if (tag.contains("pdf")) {

                                }

                                if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                                    fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                            }

                        }
                    });
                iconLin.addView(icons[icons.length - 1]);
                o++;
            }
        }

        headTxScroll.addView(iconLin);

        return headTxScroll;
    }

    public void saveInfo () {

        calledBack = "";
        if (!TxEditor.getText().toString().contains("(!")) {
            String[] splitStr = TxEditor.getText().toString().split("\n");
            int n = 0;
            for (int i = 4; i < splitStr.length; i++) {
                if (!accountAddrData[i].equals(splitStr[i])) {
                    n++;
                    break;
                }
            }
            if (n > 0) {
                TxEditor.setText(TxEditor.getText().toString().trim());

                accountAddrData = TxEditor.getText().toString().split("\n");
                fileBrowser.read_writeFileOnInternalStorage("write", "accountAddrData", "accountAddrData.txt", TxEditor.getText().toString());
            }
        }
        mainRel.removeView(selector);
        action = memoryAction;
        mainTx = memoryTx;
        mainLin.removeView(textRel);
        createTextEditorDisplay();
    }

    public void buildTxFile (String folderpath, String filename) {
        String writeTx = "";
        if(txSenderAddress != null && txSenderAddress.getText().toString().length() > 0) {
            String send = "\t\t\t\t\t";
            String[] sender = txSenderAddress.getText().toString().split("\n");
            StringBuffer strBuff = new StringBuffer();
            for (String s : sender)
                send = send + s + "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t";

            writeTx = "\n\n" + txDate.getText().toString() + send +
                    "\n\n\n" + TxEditor.getText().toString();
        } else
            writeTx = TxEditor.getText().toString();

        if(fileBrowser.read_writeFileOnInternalStorage("write", folderpath, filename+ ".txt", writeTx).length == 0)
            fileBrowser.messageStarter("Successful_TxDocumentSave", docu_Loader("Language/" + language + "/Success_TxDocumentSave.txt"),  5000);

    }

    public void createLayOverPdf () {

        String emptyTx = "";
        isPdf = true;
        if(pdfDisplayRel != null && addLayers != null) {
            if(addLayers.get(selectedPdfTab) != null &&  addLayers.get(selectedPdfTab)[pageNr] == null) {
                addLayers.get(selectedPdfTab)[pageNr] = new RelativeLayout(context);
                addLayers.get(selectedPdfTab)[pageNr].setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                addLayers.get(selectedPdfTab)[pageNr].setTag(selectedPdfTab);
                addLayers.get(selectedPdfTab)[pageNr].setFocusable(true);
                pdfDisplayRel.addView(addLayers.get(selectedPdfTab)[pageNr], pdfDisplayRel.getChildCount());
            }
            addLayers.get(selectedPdfTab)[pageNr].setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent me) {
                        switch (me.getAction()) {
                            case (MotionEvent.ACTION_DOWN): {
                                if(txarea.length > 1 && txarea[txarea.length -1] != null  && txarea[txarea.length -1].getText().toString().equals("")) {
                                    ((RelativeLayout)view).removeView(txarea[txarea.length -1]);
                                    txarea = Arrays.copyOfRange(txarea, 0, txarea.length - 1);
                                }
                                if(importImg)
                                    createAddImgView(view, me.getX(), me.getY());
                                else
                                    txRelTouched(view, me.getX(), me.getY());
                                break;
                            }
                        }
                        return false;
                    }
                });
            if(calledFrom.startsWith("import"))
               fileBrowser.messageStarter("AddLayConstruct", docu_Loader("Language/" + language + "/Instruction_AddLayConstruct.txt"), 2500);

        }
    }

    private void txRelTouched (View view, float x, float y) {

        txarea = Arrays.copyOf(txarea, txarea.length +1);
        txarea[txarea.length -1] = new EditText(context);
        txarea[txarea.length -1].setLayoutParams(new RelativeLayout.LayoutParams(pdfDisplayRel.getHeight()/32, pdfDisplayRel.getHeight()/26));
        txarea[txarea.length -1].setText("");
        txarea[txarea.length -1].setTextColor(getResources().getColor(R.color.black));
        txarea[txarea.length -1].setTextSize(6*textSize/7);
        txarea[txarea.length -1].setTag(pageNr+"-"+(txarea.length -1));
        txarea[txarea.length -1].setShowSoftInputOnFocus(false);
        txarea[txarea.length -1].setX(x);
        txarea[txarea.length -1].setY(y -20);

        txarea[txarea.length -1].requestFocus();
        ((RelativeLayout) view).addView(txarea[txarea.length -1]);

        fileBrowser.keyboardTrans = txarea[txarea.length -1];

        calledFrom = "pdfTx";

        if (fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible()) {
            int fact = displayHeight / 18,
                    fact01 = displayHeight / 18;
            if (yfact < 0.625) {
                fact = displayHeight / 28;
                fact01 = 0;
            }
            if (yfact >= 0.8) {
                fact01 = displayHeight / 12;
            }
            fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6, "softKeyBoard", null, 5, (int) (2 * displayHeight / 3 - fact),
                    displayWidth - 10, (int) (displayHeight / 3) + fact01);
        }
    }
    private void createAddImgView (View view, float x, float y) {

        calledBy = "importImg";
        calledFrom = "impImg";
        scaleFact = 1;
        pdfDisplayRel.setOnTouchListener(null);
        if (addLayers.get(selectedPdfTab) != null && addLayers.get(selectedPdfTab)[pageNr] != null)
            addLayers.get(selectedPdfTab)[pageNr].setOnTouchListener(null);

        importImgView = Arrays.copyOf(importImgView, importImgView.length + 1);
        importImgView[importImgView.length - 1] = new ImageView(fileBrowser);
        importImgView[importImgView.length - 1].setLayoutParams(new RelativeLayout.LayoutParams((displayWidth/2), RelativeLayout.LayoutParams.WRAP_CONTENT));
        importImgView[importImgView.length - 1].setTag(importImgView.length - 1 +"--"+ pageNr);
        importImgView[importImgView.length - 1].setAdjustViewBounds(true);
        importImgView[importImgView.length - 1].setX(x);
        importImgView[importImgView.length - 1].setY(y);

        importImgView[importImgView.length - 1].setImageBitmap(fileBrowser.bitmapLoader(devicePath));
        importImgView[importImgView.length - 1].setOnTouchListener(new View.OnTouchListener() {
            float x, y, preX, preY;
            int pC;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent me) {
                pC = me.getPointerCount();

                switch (me.getAction()) {
                    case (MotionEvent.ACTION_DOWN): {

                        x = me.getX();
                        y = me.getY();

                    }
                    case (MotionEvent.ACTION_MOVE): {

                        if (pC == 1) {
                            view.setY(view.getY() + (me.getY() - y));
                            view.setX(view.getX() + (me.getX() - x));
                            break;
                        } else if (pC == 2 && ((view.getHeight() * scaleFact) <= 0.25 * displayHeight) && scaleFact >= .5) {
                            scaleFact = scaleFact + (-(me.getY() - y) * 0.001);
                            if ((view.getHeight() * scaleFact) >= 0.25 * displayHeight && me.getY() < y)
                                scaleFact = scaleFact + ((me.getY() - y) * 0.001);
                            if (scaleFact <= .5 && me.getY() > y)
                                scaleFact = scaleFact + ((me.getY() - y) * 0.001);
                            view.setScaleX((float) scaleFact);
                            view.setScaleY((float) scaleFact);
                            break;
                        }
                    }
                    case (ACTION_UP): {
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException ie) {
                        }
                        break;
                    }
                }

                return true;
            }
        });

        ((RelativeLayout) view).addView(importImgView[importImgView.length - 1]);
        fileBrowser.changeIcon(fileBrowser.createTxEditor.icons[2],"TextEditorIcons","open","closed");
        importImg = false;
        mainRel.removeView(pdfDisplayLin);
        mainRel.addView(createPdfEditorDisplay(),0);
    }

    public LinearLayout createPdfEditorDisplay () {
        float f = 6;
        if(yfact <= 0.625) {
            f = 12;
        }
        mainRel.removeView(mainLin);
        mainRel.removeView(pdfDisplayLin);

        String imgPath = "";
        RelativeLayout.LayoutParams pdfDisRelParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        pdfDisRelParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pdfDisplayLin = new LinearLayout(context);
        pdfDisplayLin.setLayoutParams(pdfDisRelParam);
        pdfDisplayLin.setTag("pdfDisplayLin");

        pdfDisplayRel = new RelativeLayout(context);
        pdfDisplayRel.setLayoutParams(pdfDisRelParam);
        pdfDisplayRel.setY(displayHeight/f);

        pdfDisplayRel.setOnTouchListener(new View.OnTouchListener() {
            float x, y, preX, preY;
            int pC;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent me) {
                pC = me.getPointerCount();

                switch (me.getAction()) {
                    case (MotionEvent.ACTION_DOWN): {

                        x = me.getX();
                        y = me.getY();

                    }
                    case (MotionEvent.ACTION_MOVE): {

                        if (pC == 1) {
                            view.setY(view.getY() + (me.getY() - y));
                            view.setX(view.getX() + (me.getX() - x));
                            break;
                        } else if (pC == 2 && ((view.getHeight() * scaleFact) <= 1.75 * displayHeight) && scaleFact >= .5) {
                            scaleFact = scaleFact + (-(me.getY() - y) * 0.001);
                            if ((view.getHeight() * scaleFact) >= 1.75 * displayHeight && me.getY() < y)
                                scaleFact = scaleFact + ((me.getY() - y) * 0.001);
                            if (scaleFact <= .5 && me.getY() > y)
                                scaleFact = scaleFact + ((me.getY() - y) * 0.001);
                            view.setScaleX((float) scaleFact);
                            view.setScaleY((float) scaleFact);
                            break;
                        }
                    }
                    case (ACTION_UP): {
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException ie) {
                        }
                        break;
                    }
                }

                return true;
            }
        });

        imgView = new ImageView(context);

        scaleFact = 1;
        RelativeLayout.LayoutParams imgRelParams = new RelativeLayout.LayoutParams(txEditorLayout.getWidth(), txEditorLayout.getHeight() - txEditorLayout.getHeight() / 8);
        imgRelParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        imgView = new ImageView(context);
        imgView.setLayoutParams(imgRelParams);

        if (!newPDF && !loadedFile.equals("")) {
            openPdfStart(pageNr, new File(loadedFile));
        } else if (newPDF && addLayers.size() == 0) {
            addLayers.add(new RelativeLayout[1]);
        }
        imgView.setImageBitmap(pdfOpenBit);
        activImgView = imgView;

        pdfDisplayRel.addView(imgView);
        if (addLayers != null && addLayers.size() > 0 && addLayers.get(selectedPdfTab) != null && addLayers.get(selectedPdfTab).length == pdfPageCount) {
            try {
                if (addLayers.get(selectedPdfTab)[pageNr] != null && addLayers.get(selectedPdfTab)[pageNr].getParent() != null) {
                    ((RelativeLayout) addLayers.get(selectedPdfTab)[pageNr].getParent()).removeView(addLayers.get(selectedPdfTab)[pageNr]);
                    pdfDisplayRel.addView(addLayers.get(selectedPdfTab)[pageNr]);
                }
            } catch (ArrayIndexOutOfBoundsException ae) {}

        }

        if (pdfPageCount > 1) {
            mainRel.addView(pageList(displayWidth - displayWidth / 4, 10));
        }

        pdfDisplayLin.addView(pdfDisplayRel);

        return pdfDisplayLin;
    }

    public void generatePDFfromTx(String txt, String folder, String file) {

        StaticLayout staticLayout;
        Bitmap bmp = fileBrowser.viewToBitmap(textRel),
                scaledBitmap = Bitmap.createScaledBitmap(bmp, 210, 297, false);
        RectF rectF = new RectF(5,5, 210, 297);

        int txn = 2,
                maxLines;

        float spacingMultiplier = 1;
        float spacingAddition = 0;
        boolean includePadding = true;

        if(yfact <= 0.625) {
            txn = 3;
            maxLines = 32;
            if(!noAddr)
                maxLines = 28;
        } else {
            txn = 2;
            maxLines = 36;
            if(!noAddr)
                maxLines = 34;
        }

        PdfDocument pdfDocument;
        PdfDocument.PageInfo mypageInfo;

        String FILE = folder + "/" + file.replace(" ","") + ".pdf";

        pdfDocument = new PdfDocument();
        Paint bottomLine = new Paint(), paint = new Paint();
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(txn*textSize);
        textPaint.setLetterSpacing((float) .1);
        textPaint.setColor(getResources().getColor(R.color.black));

        bottomLine.setTextSize(textSize+4);
        bottomLine.setTextAlign(Paint.Align.CENTER);

        String[] txts = txt.split("\n");
        String[] texts = new String[0];

        for(int n=0;n<txts.length;n++) {
            if (("" + (float) n / maxLines).endsWith(".0")) {
                texts = Arrays.copyOf(texts, texts.length + 1);
                texts[texts.length -1] = "";
            }
            texts[texts.length -1] = texts[texts.length -1] + txts[n]+"\n";
        }

        for(int i=0;i<texts.length;i++) {

            textRel.removeAllViews();
            mainTx = (texts[i]);
            staticLayout = StaticLayout.Builder
                    .obtain(mainTx, 0, mainTx.length(), textPaint, bmp.getWidth()-2*displayWidth/22)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(spacingAddition, spacingMultiplier)
                    .setIncludePad(includePadding)
                    .setMaxLines(maxLines)
                    .build();

            int height = staticLayout.getHeight() +40;
            if(!noAddr && i==0)
                height = staticLayout.getHeight() +40 +headIconLin.getHeight();

            mypageInfo = new PdfDocument.PageInfo.Builder(staticLayout.getWidth() , height, (i+1)).create();
            PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

            Canvas canvas = myPage.getCanvas();
            if(!noAddr && i==0) {
                headIconLin.draw(canvas);
                canvas.translate(10,headIconLin.getHeight());
                maxLines = maxLines +4;
            }
            if(isBackground) {
                canvas.drawBitmap(bmp, 10, 10, paint);
                textRel.draw(canvas);
                canvas.translate(10,bmp.getHeight());
            } else {
                //canvas.drawBitmap(scaledBitmap, null, rectF, paint);
                canvas.translate(20,20);
                staticLayout.draw(canvas);
            }

            pdfDocument.finishPage(myPage);
        }

        try {
            pdfDocument.writeTo(new FileOutputStream(FILE));
            loadedFile = FILE;
            kindOfFormat = ".pdf";
            if(fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible())
                fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);
            fileBrowser.messageStarter("Successful_PdfDocumentSave", docu_Loader("Language/" + language + "/Success_PdfDocumentSave.txt"),  5000);

        } catch (IOException e) {

            String[] noSuccessful = docu_Loader("Language/" + language + "/Unsuccessful_Action.txt"),
                    noSuccsess = new String[]{e.getMessage()};
            for (String s : noSuccessful) {
                noSuccsess = Arrays.copyOf(noSuccsess, noSuccsess.length + 1);
                noSuccsess[noSuccsess.length - 1] = s;
            }
            if(fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible())
                fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);

            fileBrowser.messageStarter("Instruction", noSuccsess, 5000);
        }

        pdfDocument.close();


    }
    public void generatePDFfromPdf(String folder, String file) {
        Bitmap bmp;
        PdfDocument pdfDocument;
        PdfDocument.PageInfo mypageInfo;
        Canvas canvas;

        String FILE = folder + "/" + file.replace(" ","") + ".pdf";
        Paint paint = new Paint();
        pdfDocument = new PdfDocument();

        if (caller.endsWith("New"))
            pdfPageCount = 1;

        for(int i=0;i<pdfPageCount;i++) {

            pdfDisplayRel.removeAllViews();
            if(!(caller.endsWith("NEW") && pdfPageCount == 1)) {
                openPdfStart(i, new File(loadedFile));
                imgView.setImageBitmap(pdfOpenBit);

                pdfDisplayRel.addView(imgView);
            }

            if (addLayers != null && addLayers.get(selectedPdfTab) != null && addLayers.get(selectedPdfTab)[i] != null) {
                if(addLayers.get(selectedPdfTab)[i].getParent() != null)
                    ((RelativeLayout) addLayers.get(selectedPdfTab)[i].getParent()).removeView(addLayers.get(selectedPdfTab)[i]);

                pdfDisplayRel.addView(addLayers.get(selectedPdfTab)[i]);

            }

            if(isBackground)
                bmp = fileBrowser.viewToBitmap(textRel);
            else
                bmp = fileBrowser.viewToBitmap(pdfDisplayRel);

            paint.reset();
            //mypageInfo = new PdfDocument.PageInfo.Builder(bmp.getWidth(), (bmp.getHeight() - bmp.getHeight()/11), i).create();
            mypageInfo = new PdfDocument.PageInfo.Builder(bmp.getWidth(), bmp.getHeight(), i).create();

            PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

            canvas = myPage.getCanvas();
            canvas.drawBitmap(bmp, 0, 20, paint);
            if(isBackground) {
                canvas.drawBitmap(bmp, 20, 20, paint);
                textRel.draw(canvas);
            } else {
                Bitmap scbmp = Bitmap.createScaledBitmap(bmp, 210, 297, false);
                canvas.drawBitmap(scbmp, 20, 20, paint);
                pdfDisplayRel.draw(canvas);
            }
            pdfDocument.finishPage(myPage);

        }

        try {
            pdfDocument.writeTo(new FileOutputStream(FILE));

            pdfDocument.close();
            loadedFile = FILE;
            fileBrowser.messageStarter("Successful_PdfDocumentSave", docu_Loader("Language/" + language + "/Success_PdfDocumentSave.txt"),  5000);

        } catch (Exception e) {
            String[] noSuccessful = docu_Loader("Language/" + language + "/Unsuccessful_Action.txt"),
                    noSuccsess = new String[]{e.getMessage()};
            for (String s : noSuccessful) {
                noSuccsess = Arrays.copyOf(noSuccsess, noSuccsess.length + 1);
                noSuccsess[noSuccsess.length - 1] = s;
            }
            if(fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible())
                fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);
            fileBrowser.messageStarter("Instruction", noSuccsess, 5000);
        }

    }

    public boolean Merge_PdfFiles(String folder,String file, String[] SourceFiles) {

        try {
            int f = 0;
            PdfReader reader = new PdfReader(SourceFiles[f]);
            int n = reader.getNumberOfPages();

            Document document = new Document(reader.getPageSizeWithRotation(1));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfImportedPage page;
            int rotation;

            while (f < SourceFiles.length) {
                int i = 0;
                while (i < n) {
                    i++;
                    document.setPageSize(reader.getPageSizeWithRotation(i));
                    document.newPage();
                    page = writer.getImportedPage(reader, i);
                    rotation = reader.getPageRotation(i);
                    if (rotation == 90 || rotation == 270) {
                        cb.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).getHeight());
                    } else {
                        cb.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
                    }
                }
                f++;
                if (f < SourceFiles.length) {
                    reader = new PdfReader(SourceFiles[f]);
                    n = reader.getNumberOfPages();
                }
            }

            document.close();
            loadedFile = file;
            fileBrowser.messageStarter("Successful_PdfDocumentSave", docu_Loader("Language/" + language + "/Success_PdfDocumentSave.txt"),  5000);

            //fileBrowser.reloadFileBrowserDisplay();
        } catch (Exception e) {
            fileBrowser.messageStarter("Instruction", docu_Loader("Language/" + language + "/Unsuccessful_Action.txt"),  5000);
        }
        return true;
    }
    public ScrollView pageList (int x, int y) {
        fileBrowser.isPdf = false;
        mainRel.removeView(scView);

        arrayList = new ArrayList<>(0);
        String page = "Seite";
        if(language.equals("English")) page = "Page";
        for (int i = 1; i <= fileBrowser.pdfPageCount; i++)
            arrayList.add(new String[]{page + " " + (i)});



        int height = arrayList.size() * displayHeight/54;
        if(arrayList.size() > 3)
            height = displayHeight/18;

        scView = new ScrollView(context);
        scView.setLayoutParams(new FrameLayout.LayoutParams(displayWidth/5 +10,height +20));
        scView.setBackgroundColor(getResources().getColor(R.color.grey_blue_overlay));
        scView.setPadding(5,5,5,5);
        scView.post(new Runnable() {
            public void run() {
                scView.smoothScrollBy(0, scY[selectedTab]);
            }
        });
        scView.setX(x);
        scView.setY(y);

        LinearLayout pgLin = new LinearLayout(context);
        pgLin.setLayoutParams(new LinearLayout.LayoutParams(displayWidth/5,height));
        pgLin.setOrientation(LinearLayout.VERTICAL);
        pgLin.setBackgroundColor(getResources().getColor(R.color.white));
        pgLin.setPadding(15,5,5,15);

        txPages = new TextView[arrayList.size()];
        for(int i=0;i<txPages.length;i++) {
            txPages[i] = new TextView(context);
            txPages[i].setText(arrayList.get(i)[0]);
            txPages[i].setTextSize(textSize);
            txPages[i].setTextColor(getResources().getColor(R.color.black));
            if(pageNr == i)
                txPages[i].setTextColor(getResources().getColor(R.color.green));
            txPages[i].setTag(i);
            txPages[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileBrowser.changeIcon(fileBrowser.createTxEditor.icons[2],"TextEditorIcons","open","closed");

                    calledFrom = "pageChoose";
                    int tag = Integer.parseInt(v.getTag().toString());
                    for(int i=0;i<txPages.length;i++)
                        txPages[i].setTextColor(getResources().getColor(R.color.black));
                    ((TextView)v).setTextColor(getResources().getColor(R.color.green));
                    selectedTx = tag;
                    scY[selectedTab] = scView.getScrollY();
                    pageNr = tag;

                    mainRel.removeView(pdfDisplayLin);
                    mainRel.addView(createPdfEditorDisplay(), mainRel.getChildCount() - 4);

                }
            });
            pgLin.addView(txPages[i]);
        }

        scView.addView(pgLin);

        return scView;
    }
    private void createAddedLayer (int pg) {

        if(addLayers != null && addLayers.get(selectedPdfTab)[pageNr] != null && pdfDisplayRel != null && !calledFrom.equals("impImg")) {
            if (Integer.parseInt(addLayers.get(selectedTab)[pageNr].getTag().toString()) != pg)
                pdfDisplayRel.removeView(addLayers.get(selectedPdfTab)[pageNr]);
        }
        if(addLayers != null && addLayers.get(selectedPdfTab)[pageNr] != null && ((!calledFrom.equals("savPdf") && pg != pageNr) || (calledFrom.equals("savPdf") && pg == pageNr))) {
            pdfDisplayRel.addView(addLayers.get(selectedPdfTab)[pageNr]);
        }

        calledFrom = "";
    }

    public void openPdfStart(int pg, File f) {

        startOpenPdf = new enterOpenPdf(pg,f);
        startOpenPdf.start();
        try {
            startOpenPdf.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    class enterOpenPdf extends Thread {
        int pageNumber;
        File pdfFile;
        public enterOpenPdf (int pg, File file) { pageNumber = pg; pdfFile = file;}

        public void run() {
            openPdf();
        }

        public void openPdf() {
            Bitmap bitmap = null;
            ParcelFileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = fileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);

                PdfRenderer mPdfRenderer;
                PdfRenderer.Page mPdfPage;

                mPdfRenderer = new PdfRenderer(fileDescriptor);

                pdfPageCount = mPdfRenderer.getPageCount();

                pageNr = pageNumber;
                refreshToFillIn();
                mPdfPage = mPdfRenderer.openPage(pageNr);

                bitmap = Bitmap.createBitmap(mPdfPage.getWidth(),
                        mPdfPage.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mPdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                fileDescriptor.close();
                refreshToDefine();

                if(caller.endsWith("New") && selectedPdfTab < addLayers.size() && addLayers.get(selectedPdfTab).length == 0) {
                    addLayers.set(selectedPdfTab, new RelativeLayout[pdfPageCount]);
                    calledFrom = "openPdf";
                    createLayOverPdf();
                }

            } catch (IOException io) {

                //addLayers.set(selectedPdfTab,new RelativeLayout[1]);
                fileBrowser.messageStarter("Instruction", new String[]{io.getMessage()},  3500);

            }

            loadedFile = pdfFile.getPath();
            pdfOpenBit = bitmap;
        }

    }
}
