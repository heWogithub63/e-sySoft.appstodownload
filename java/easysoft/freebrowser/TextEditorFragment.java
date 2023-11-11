package easysoft.freebrowser;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
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
    RelativeLayout[] pdfTxLays;
    LinearLayout headIconLin, mainLin, iconLin;
    LinearLayout pdfDisplayLin;
    RelativeLayout pdfDisplayRel;
    LinearLayout scaleLin;
    ScrollView pdfScroll, scView;
    int pageNr = 0, selectedTx = 0, scY;
    HorizontalScrollView headTxScroll;

    enterOpenPdf startOpenPdf;
    Bitmap pdfOpenBit;

    static EditText TxEditor;
    TextView txDate, txSenderAddress;
    TextView[] txPages;
    ImageView[] icons;
    ImageView  imgView, activImgView;
    ImageView[] importImgView;
    LinearLayout TextLin;
    String caller = "", call = "", mainTx = "", headerTx = "", kindOfFormat = "", action = "", memoryAction = "", pdfFileUrl = "";
    String[] readedText, accountAddrData;
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

            if(kindOfFormat.equals(".txt") && !caller.endsWith("New"))
                for(String s: readedText) {
                    if(s.startsWith("An: ") || s.startsWith("To: ")) {
                        headerTx = mainTx;
                        mainTx = "";
                    }
                    mainTx = mainTx + s + "\n";
                }
            else if(kindOfFormat.equals(".pdf")&& !caller.endsWith("New")) {
                scaleFact = 1.0;
                selectedTx_01 = 0;
                pdfFileUrl = readedText[0];
            }

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        view = inflater.inflate(R.layout.fragment_text_editor, container, false);
        mainRel = (RelativeLayout) view.findViewById(R.id.textEditorMainRel);
        mainRel.setBackgroundColor(getResources().getColor(R.color.white));

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
        if(kindOfFormat.equals(".txt"))
            mainRel.addView(createTextEditorDisplay(createHaderIcons()));
        else if(kindOfFormat.equals(".pdf")) {
            mainRel.addView(createPdfEditorDisplay(createHaderIcons()));
            if(pdfPageCount > 1)
                mainRel.addView(pageList(displayWidth -displayWidth/4,10));
        }
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

                if (devicePath != null && devicePath.length() > 0)
                    fileBrowser.reloadFileBrowserDisplay();
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

        String[] strings = (TxEditor.getText().toString()).split("\n");
        mainTx = "";

        for(String s : strings) {
            if (s.startsWith("An: ") || s.startsWith("To: ")) {
                headerTx = mainTx;
                mainTx = "";
            }
            mainTx = mainTx + s + "\n";
        }
    }


    public LinearLayout createTextEditorDisplay(LinearLayout mainLin) {
        int factx = (int)(50*xfact),
                facty = (int)(30*yfact);

        if(textRel != null)
            mainLin.removeView(textRel);

        textRel = new RelativeLayout(context);
        textRel.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

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
        TextLin.setLayoutParams(new RelativeLayout.LayoutParams(txEditorLayout.getWidth() - factx,  txEditorLayout.getHeight() - (facty)));
        TextLin.setPadding(txEditorLayout.getWidth()/16,txEditorLayout.getHeight()/28, txEditorLayout.getWidth()/18,txEditorLayout.getHeight()/16);
        TextLin.setOrientation(LinearLayout.HORIZONTAL);

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
            if(mainTx.contains(headerTx))
                mainTx = mainTx.substring(headerTx.length());

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

                if (mainTx.length() == 0) {
                    String[] headLines = fileBrowser.docu_Loader("Language/" + fileBrowser.language + "/MailHeadLines.txt");
                    for (int i = 1; i < headLines.length; i++) {
                        if (i < headLines.length - 1)
                            mainTx = mainTx +headLines[i] + "\n\n";
                        else
                            mainTx = mainTx +headLines[i] + "\n";
                    }
                }
            }

            headIconLin.addView(headiconRel);
            headIconLin.setY(displayHeight/62);
            textRel.addView(headIconLin);
            TextLin.setY(headiconRelParam.height);

        } else {
            TextLin.setY(0);
            mainTx = headerTx + mainTx;
            headerTx = "";
        }

        TxEditor = new EditText(context);
        TxEditor.setTextColor(getResources().getColor(R.color.black));
        TxEditor.setText(mainTx);
        TxEditor.setTextSize(textSize);
        TxEditor.setPadding(10,10,10,10);
        TxEditor.setGravity(Gravity.TOP);
        TxEditor.setShowSoftInputOnFocus(false);
        TxEditor.setDrawingCacheEnabled(true);

        if(caller.endsWith("New"))
            TxEditor.requestFocus();
        TxEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fact = displayHeight/18,
                        fact01 = displayHeight/18;
                if(yfact < 0.625) {
                    fact = displayHeight / 28;
                    fact01 = 0;
                }
                if(yfact >= 0.8) {
                    fact01 = displayHeight/12;
                }
                fileBrowser.keyboardTrans = ((EditText) v);
                if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible())
                    fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6,"softKeyBoard",null,5,(int)(2*displayHeight/3 -fact),
                            displayWidth -10, (int)(displayHeight/3) +fact01);
            }
        });

        if (TxEditor.getText().toString().contains("(!") && TxEditor.getText().toString().contains("!)")) {

            selector = new ImageView(context);
            selector.setLayoutParams(new RelativeLayout.LayoutParams((int) (80 * xfact), (int) (80 * xfact)));
            selector.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/txEditorSelector.png"));
            selector.setX(3 * displayWidth / 5);
            selector.setY(displayHeight / 3);

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

        txScroll.addView(TxEditor);
        TextLin.addView(txScroll);
        textRel.addView(TextLin);
        mainLin.addView(textRel);

        calledBy = "";
        return mainLin;
    }

    public LinearLayout createHaderIcons () {

        icons = new ImageView[0];
        mainLin = new LinearLayout(context);
        mainLin.setOrientation(LinearLayout.VERTICAL);
        mainLin.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, displayHeight -displayHeight/12));
        mainLin.setPadding(15,0,0,0);
        mainLin.setBackgroundColor(getResources().getColor(R.color.white));


        headTxScroll = new HorizontalScrollView(context);
        headTxScroll.setHorizontalScrollBarEnabled(false);
        headTxScroll.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth -10, RelativeLayout.LayoutParams.WRAP_CONTENT));
        headTxScroll.setBackgroundColor(getResources().getColor(R.color.white_overlay));
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
                        (caller.equals("pdfEditorDisplayNew") && !s.contains("pdf"))) {
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
                                    //refreshToFillIn();
                                    //createTextEditorDisplay(mainLin);

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
                                System.err.println(caller+"...."+tag);
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
                                if(devicePath.length() > 0 && devicePath.contains("/")) {
                                    if(devicePath.substring(devicePath.lastIndexOf("/")).contains("."))
                                        devicePath = devicePath.substring(0, devicePath.lastIndexOf("/"));

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
        mainLin.addView(headTxScroll);

        return mainLin;
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
        createTextEditorDisplay(mainLin);
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

    public void createTxOverPdf () {

        String emptyTx = "";
        isPdf = true;

        if(pdfDisplayRel != null && pdfTxLays != null) {

            pdfTxLays[pageNr] = new RelativeLayout(context);
            pdfTxLays[pageNr].setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
            pdfTxLays[pageNr].setTag(pageNr);
            pdfTxLays[pageNr].setFocusable(true);

            pdfTxLays[pageNr].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent me) {
                    switch (me.getAction()) {
                        case (MotionEvent.ACTION_DOWN): {
                            if(txarea.length > 1 && txarea[txarea.length -1] != null  && txarea[txarea.length -1].getText().toString().equals("")) {
                                ((RelativeLayout)view).removeView(txarea[txarea.length -1]);
                                txarea = Arrays.copyOfRange(txarea, 0, txarea.length - 1);
                            }
                            txRelTouched(view, me.getX(), me.getY());
                            break;
                        }
                    }
                    return false;
                }
            });

            pdfDisplayRel.addView(pdfTxLays[pageNr], pdfDisplayRel.getChildCount());
        }
    }

    private void txRelTouched (View view, float x, float y) {

        txarea = Arrays.copyOf(txarea, txarea.length +1);
        txarea[txarea.length -1] = new EditText(context);
        txarea[txarea.length -1].setLayoutParams(new RelativeLayout.LayoutParams(pdfDisplayRel.getHeight()/32, pdfDisplayRel.getHeight()/23));
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

    public LinearLayout createPdfEditorDisplay (LinearLayout mainLin) {
        String imgPath = "";
        Bitmap bitmap = null;
        RelativeLayout.LayoutParams pdfDisRelParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (activImgView != null) {
            pdfDisplayRel.removeAllViews();
            pdfDisplayLin.removeAllViews();
            mainLin.removeView(pdfDisplayLin);
        } else {
            pdfDisplayRel = new RelativeLayout(context);
            pdfDisplayRel.setLayoutParams(pdfDisRelParam);
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
                            } else if (pC == 2 && ((view.getHeight() * scaleFact) <= 1.75*displayHeight) && scaleFact >= 1) {
                                scaleFact = scaleFact + (-(me.getY() - y) * 0.001);
                                if ((view.getHeight() * scaleFact) >= 1.75*displayHeight && me.getY() < y)
                                    scaleFact = scaleFact + ((me.getY() - y) * 0.001);
                                if (scaleFact <= 1 && me.getY() > y)
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


            pdfDisplayLin = new LinearLayout(context);
            pdfDisplayLin.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        }

        if (importImg)
            imgPath = devicePath;

        if (importImg) {
            calledBy = "importImg";
            calledFrom = "impImg";
            scaleFact = 1;
            pdfDisplayRel.setOnTouchListener(null);
            if(pdfTxLays != null)
                pdfTxLays[pageNr].setOnTouchListener(null);

            importImgView[pageNr] = new ImageView(context);
            importImgView[pageNr].setLayoutParams(new RelativeLayout.LayoutParams((displayWidth/2), RelativeLayout.LayoutParams.WRAP_CONTENT));
            importImgView[pageNr].setTag(pageNr);
            importImgView[pageNr].setAdjustViewBounds(true);
            importImgView[pageNr].setX(displayWidth/4);
            importImgView[pageNr].setY(displayHeight/3);

            importImgView[pageNr].setImageBitmap(fileBrowser.bitmapLoader(imgPath));
            importImgView[pageNr].setOnTouchListener(new View.OnTouchListener() {
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
                            } else if (pC == 2 && ((view.getHeight() * scaleFact) <= 0.25*displayHeight) && scaleFact >= .5) {
                                scaleFact = scaleFact + (-(me.getY() - y) * 0.001);
                                if ((view.getHeight() * scaleFact) >= 0.25*displayHeight && me.getY() < y)
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

        }
        openPdfStart(pageNr,new File(pdfFileUrl));
        bitmap = pdfOpenBit;
        imgView = new ImageView(context);

        scaleFact = 1;
        RelativeLayout.LayoutParams imgRelParams = new RelativeLayout.LayoutParams(txEditorLayout.getWidth(), txEditorLayout.getHeight() -txEditorLayout.getHeight()/8);
        imgRelParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        imgView = new ImageView(context);
        imgView.setLayoutParams(imgRelParams);
        imgView.setImageBitmap(bitmap);

        activImgView = imgView;

        pdfDisplayRel.addView(imgView);
        if(pdfTxLays != null && pdfTxLays[pageNr] != null)
            pdfDisplayRel.addView(pdfTxLays[pageNr]);

        if(importImg)
        {
            pdfDisplayRel.addView(importImgView[pageNr]);
            activImgView = importImgView[pageNr];

            fileBrowser.changeIcon(fileBrowser.createTxEditor.icons[0],"TextEditorIcons", "closed", "open");
            icons[3].setEnabled(true);
            icons[4].setEnabled(true);

            importImg = false;
        }

        pdfDisplayLin.addView(pdfDisplayRel);
        mainLin.addView(pdfDisplayLin);

        return mainLin;
    }

    public void generatePDFfromTx(String[] txts, String folder, String file) {
        StaticLayout staticLayout;
        Bitmap bmp = fileBrowser.viewToBitmap(textRel);
        int pageWidth = bmp.getWidth(),
                pageHeight  = bmp.getHeight(),
                txn = 2,
                maxLines = 36;
        float spacingMultiplier = 1;
        float spacingAddition = 0;
        boolean includePadding = true;

        if(yfact < 0.625) {
            txn = 3;
            maxLines = 24;
        }

        PdfDocument pdfDocument;
        PdfDocument.PageInfo mypageInfo;

        String FILE = folder + "/" + file.replace(" ","") + ".pdf";

        pdfDocument = new PdfDocument();
        Paint bottomLine = new Paint(), paint = new Paint();
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(txn*textSize);
        textPaint.setColor(0xFF000000);

        bottomLine.setTextSize(textSize+4);
        bottomLine.setTextAlign(Paint.Align.CENTER);

        for(int i=0;i<txts.length;i++) {
            textRel.removeAllViews();

            mainTx = (txts[i]);
            staticLayout = StaticLayout.Builder
                    .obtain(mainTx, 0, mainTx.length(), textPaint, pageWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(spacingAddition, spacingMultiplier)
                    .setIncludePad(includePadding)
                    .setMaxLines(maxLines)
                    .build();

            mypageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, (i+1)).create();
            PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

            Canvas canvas = myPage.getCanvas();
            if(!noAddr && i==0) {
                headIconLin.draw(canvas);
                canvas.translate(0,headIconLin.getHeight());
            }
            if(isBackground) {
                canvas.drawBitmap(bmp, 40, 0, paint);
                textRel.draw(canvas);
                canvas.translate(0,textRel.getHeight());
            } else {
                staticLayout.draw(canvas);
            }

            pdfDocument.finishPage(myPage);
        }

        try {
            pdfDocument.writeTo(new FileOutputStream(FILE));
            devicePath = FILE;
            kindOfFormat = ".pdf";
            if(fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible())
                fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);
            fileBrowser.messageStarter("Successful_TxDocumentSave", docu_Loader("Language/" + language + "/Success_TxDocumentSave.txt"),  5000);

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

        if(pdfPageCount == 0 && importImgView.length >0);
           pdfPageCount = 1;
        pageNr = 0;
        int pg = 0;
        for(int i=0;i<pdfPageCount;i++) {
            calledFrom = "savPdf";
            pdfDisplayRel.removeAllViews();
            openPdfStart(i,new File(pdfFileUrl));

            imgView.setImageBitmap(pdfOpenBit);
            pdfDisplayRel.addView(imgView);
            createAddedLayer(pg);

            if(isBackground)
                bmp = fileBrowser.viewToBitmap(textRel);
            else
                bmp = fileBrowser.viewToBitmap(pdfDisplayRel);

            paint.reset();
            mypageInfo = new PdfDocument.PageInfo.Builder(bmp.getWidth(), (bmp.getHeight() - bmp.getHeight()/11), i).create();
            PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

            canvas = myPage.getCanvas();
            canvas.drawBitmap(bmp, 0, 20, paint);
            if(isBackground)
                textRel.draw(canvas);
            else
                pdfDisplayRel.draw(canvas);

            pdfDocument.finishPage(myPage);
          pg++;
        }

        try {
            pdfDocument.writeTo(new FileOutputStream(FILE));

            pdfDocument.close();
            devicePath = FILE;
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
        String pdf = ".pdf";
        if(file.endsWith(".pdf"))
            pdf="";
        String Destination = folder +"/"+ file.replace(" ","") +pdf;

        try {
            int f = 0;
            PdfReader reader = new PdfReader(SourceFiles[f]);
            int n = reader.getNumberOfPages();

            Document document = new Document(reader.getPageSizeWithRotation(1));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(Destination));

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
            devicePath = Destination;
            fileBrowser.messageStarter("Successful_PdfDocumentSave", docu_Loader("Language/" + language + "/Success_PDFDocumentSave.txt"),  5000);

            //fileBrowser.reloadFileBrowserDisplay();
        } catch (Exception e) {
            fileBrowser.messageStarter("Instruction", docu_Loader("Language/" + language + "/Unsuccessful_Action.txt"),  5000);
        }
        return true;
    }
    public ScrollView pageList (int x, int y) {
        fileBrowser.isPdf = false;
        arrayList = new ArrayList<>(0);
        String page = "Seite";
        if(language.equals("English")) page = "Page";
        for (int i = 1; i <= fileBrowser.pdfPageCount; i++)
            arrayList.add(new String[]{page + " " + (i)});



        int height = arrayList.size() * 5*textSize;
        if(arrayList.size() > 6)
            height = displayHeight/9;

        scView = new ScrollView(context);
        scView.setLayoutParams(new FrameLayout.LayoutParams(displayWidth/5 +10,height +10));
        scView.setBackgroundColor(getResources().getColor(R.color.grey_blue_overlay));
        scView.setPadding(5,5,5,5);
        scView.post(new Runnable() {
            public void run() {
                scView.smoothScrollBy(0, scY);
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
            if(selectedTx == i)
                txPages[i].setTextColor(getResources().getColor(R.color.green));
            txPages[i].setTag(i);
            txPages[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int tag = Integer.parseInt(v.getTag().toString());
                    for(int i=0;i<txPages.length;i++)
                        txPages[i].setTextColor(getResources().getColor(R.color.black));
                    ((TextView)v).setTextColor(getResources().getColor(R.color.green));
                    selectedTx = tag;
                    scY = scView.getScrollY();
                    openPdfStart(tag,new File(pdfFileUrl));
                    imgView.setImageBitmap(pdfOpenBit);
                }
            });
            pgLin.addView(txPages[i]);
        }

        scView.addView(pgLin);

        return scView;
    }
    private void createAddedLayer (int pg) {

        if(pdfTxLays != null && pdfTxLays.length > 0 && pdfTxLays[pageNr] != null && pdfDisplayRel != null && !calledFrom.equals("impImg")) {
            if (pdfTxLays[pageNr] != null && Integer.parseInt(pdfTxLays[pageNr].getTag().toString()) != pg)
                pdfDisplayRel.removeView(pdfTxLays[pageNr]);
        }
        if((pdfTxLays != null && pdfTxLays[pg] != null) && ((!calledFrom.equals("savPdf") && pg != pageNr) || (calledFrom.equals("savPdf") && pg == pageNr))) {
            Log.e("Drin", "<--");
            pdfDisplayRel.addView(pdfTxLays[pg]);
        }

        if(importImgView != null && importImgView.length > 0 && importImgView[pageNr] != null && pdfDisplayRel != null && !calledFrom.equals("pdfTx")) {
            if (importImgView[pageNr] != null && Integer.parseInt(importImgView[pageNr].getTag().toString()) != pg)
                pdfDisplayRel.removeView(importImgView[pageNr]);
        }
        if((importImgView != null && importImgView[pg] != null) && ((!calledFrom.equals("savPdf") && pg != pageNr) || (calledFrom.equals("savPdf") && pg == pageNr))) {
            Log.e("Drin", "<::");
            pdfDisplayRel.addView(importImgView[pg]);
        }

        calledFrom = "";
    }

    public void openPdfStart(int pg, File f) {
        if(!calledFrom.equals("savPdf"))
            createAddedLayer(pg);
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

            ParcelFileDescriptor fileDescriptor = null;
            Bitmap bitmap = null;
            try {
                fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);

                PdfRenderer mPdfRenderer;
                PdfRenderer.Page mPdfPage;

                mPdfRenderer = new PdfRenderer(fileDescriptor);

                pdfPageCount = mPdfRenderer.getPageCount();
                pageNr = pageNumber;

                if(pageNr == 0 && pdfTxLays == null && importImgView == null) {
                    importImgView = new ImageView[pdfPageCount];
                    pdfTxLays = new RelativeLayout[pdfPageCount];
                }

                mPdfPage = mPdfRenderer.openPage(pageNumber);

                bitmap = Bitmap.createBitmap(mPdfPage.getWidth(),
                        mPdfPage.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mPdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                fileDescriptor.close();
            } catch (IOException io) {
                Log.e("fileDescriptor",io.getMessage());

            }

            devicePath = pdfFile.getPath();

            pdfOpenBit = bitmap;
        }

    }
}
