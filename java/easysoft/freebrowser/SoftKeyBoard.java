package easysoft.freebrowser;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.Arrays;

import static easysoft.freebrowser.FileBrowser.*;

public class SoftKeyBoard extends Fragment {
    ClipboardManager clipBoard;

    View view;
    ImageView Okicon;
    FrameLayout keyBoardLayout;
    ArrayList<String[]> smallTabsTx;
    ArrayList<String[]> largeTabsTx;
    RelativeLayout keyBoardMainRel;
    EditText txEd;

    boolean shift = false, shiftFixed = false;
    String kindOf_keys = "";

    float previousY = 0;
    int startPointer = 0, endPointer = 0;
    String kindOfSize = "", collectionStr = "";

    ImageButton[] keyBordButtons = new ImageButton[0];

    public SoftKeyBoard() {
    }

    public static SoftKeyBoard newInstance() {
        SoftKeyBoard fragment = new SoftKeyBoard();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        smallTabsTx = new ArrayList<>(0);
        smallTabsTx.add(new String[]{"@", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "ß", "Back"});
        smallTabsTx.add(new String[]{"Tab", "q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "ü", "+"});
        smallTabsTx.add(new String[]{"VVVV", "a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä", "*"});
        smallTabsTx.add(new String[]{"Clear", "y", "x", "c", "v", "b", "n", "m", ",", ".", "-", "Enter"});
        smallTabsTx.add(new String[]{"Paste", "< ", " >", "  TAB  ",  ">>", "<<", "#", "Copy", "Shift"});

        largeTabsTx = new ArrayList<>(0);
        largeTabsTx.add(new String[]{"!", "'", "§", "$", "%", "&", "/", "(", ")", "=", "?", "[", "Back",});
        largeTabsTx.add(new String[]{"{ }", "Q", "W", "E", "R", "T", "Z", "U", "I", "O", "P", "Ü", "]"});
        largeTabsTx.add(new String[]{"vvvv", "A", "S", "D", "F", "G", "H", "J", "K", "L", "Ö", "Ä", "'"});
        largeTabsTx.add(new String[]{"Clear", "Y", "X", "C", "V", "B", "N", "M", ";", ":", "_", "Enter"});
        largeTabsTx.add(new String[]{"Paste", "°", "< ", " >", "  TAB  ",  "vv", "^^", "Copy", "Shift"});

        clipBoard = (ClipboardManager) fileBrowser.getSystemService(Context.CLIPBOARD_SERVICE);
        kindOf_keys = "small";

        keyBoardLayout = fileBrowser.frameLy.get(6);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_soft_key_board, container, false);
        keyBoardMainRel = (RelativeLayout) view.findViewById(R.id.keyboardMainRel);

        keyBoardLayout.setOnTouchListener(new View.OnTouchListener() {
            float y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent me) {

                switch (me.getAction()) {
                    case (MotionEvent.ACTION_DOWN): {
                        y = me.getY();
                        break;
                    }
                    case (MotionEvent.ACTION_MOVE): {
                        if((keyBoardLayout.getY() +me.getY() /10) < displayHeight -displayHeight/8)
                            v.setY(v.getY() + (me.getY() -y));
                        break;
                    }
                }

                return true;
            }
        });
        createKeyboard(smallTabsTx);
        keyBoardMainRel.addView(createKeyboardIcon());


        keyBoardLayout.bringToFront();
        return view;
    }

    private LinearLayout createKeyboardIcon () {

        int f = 20;
        if(yfact <= 0.625)
            f= 22;
        LinearLayout KeyboardIconLin = new LinearLayout(fileBrowser);
        KeyboardIconLin.setOrientation(LinearLayout.HORIZONTAL);
        KeyboardIconLin.setLayoutParams(new RelativeLayout.LayoutParams((int)(displayWidth/6), (int)(displayHeight/f)));
        KeyboardIconLin.setPadding(5,5,5,5);
        KeyboardIconLin.setX(keyBoardLayout.getWidth() - displayWidth/6);
        KeyboardIconLin.setY(-10);

        RelativeLayout.LayoutParams kbIcPa = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        kbIcPa.addRule(RelativeLayout.CENTER_VERTICAL);
        RelativeLayout.LayoutParams kbIcPa1 = new RelativeLayout.LayoutParams(displayWidth/12,displayWidth/14);
        kbIcPa1.addRule(RelativeLayout.CENTER_VERTICAL);
        if(fileBrowser.fragId != 8) {
            Okicon = new ImageView(fileBrowser);
            Okicon.setLayoutParams(kbIcPa1);
            Okicon.setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/letterOK_open.png"));
            Okicon.setTag("OkIcon letterOK_closed.png");
            Okicon.setPadding(0, 0, 10, 0);

            Okicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible()) {
                        fileBrowser.showMessage.clickOk();
                    }
                    if ((fileBrowser.fragId == 5 || fileBrowser.fragId == 7) && calledBack.equals("InfoView")) {
                        switch (fileBrowser.fragId) {
                            case (5): {
                                fileBrowser.createSendEmail.saveInfo();
                                fileBrowser.createSendEmail.createNewDisplay();
                                fileBrowser.changeIcon(fileBrowser.createSendEmail.icons[fileBrowser.createSendEmail.icons.length - 1],
                                        "mailIcons", "open", "closed");
                                fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
                                break;
                            }
                            case (7): {
                                fileBrowser.changeIcon(fileBrowser.createTxEditor.icons[fileBrowser.createTxEditor.icons.length - 1],
                                        "TextEditorIcons", "open", "closed");
                                fileBrowser.createTxEditor.saveInfo();
                                break;
                            }
                        }
                    }
                }
            });

            KeyboardIconLin.addView(Okicon);
        } else if(fileBrowser.fragId == 8 && (calledFrom.contains("Search") ||
                (fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible()))) {

            ImageView searchIcon = new ImageView(fileBrowser);
            searchIcon.setLayoutParams(kbIcPa1);
            searchIcon.setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/search_closed.png"));
            searchIcon.setTag("searchIcon search_closed.png");
            searchIcon.setPadding(0, 0, 10, 0);

            searchIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    fileBrowser.webBrowserDisplay.timeImage.setVisibility(View.VISIBLE);
                    fileBrowser.webBrowserDisplay.timerAnimation.start();

                    if (fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible())
                        fileBrowser.showMessage.clickOk();
                    else {
                        String tx = txEd.getText().toString();
                        if (!tx.equals((""))) {
                            tx = tx.replace(" ", "+");

                            String querry = "search";
                            tx = fileBrowser.webBrowserDisplay.runningUrl + querry + "?" + calledFrom.substring(calledFrom.lastIndexOf("_") + 1) + "=" + tx;
                            try {
                                fileBrowser.webBrowserDisplay.webView.loadUrl(tx);
                            } catch (Exception e) {
                                tx = fileBrowser.webBrowserDisplay.runningUrl.substring(0, fileBrowser.webBrowserDisplay.runningUrl.lastIndexOf("/")) + "?search=" + tx;
                                fileBrowser.webBrowserDisplay.webView.loadUrl(tx);
                            }

                        }
                        calledFrom = "KeyBoard";
                    }
                }
            });

            KeyboardIconLin.addView(searchIcon);
        }

        ImageView keyboardicon = new ImageView(fileBrowser);
        keyboardicon.setLayoutParams(kbIcPa);
        keyboardicon.setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/keyboard.png"));
        keyboardicon.setTag("keyboardIcon keyboard_closed.png");

        keyboardicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible() && fileBrowser.showMessage.requestedText != null) {
                    threadStop = true;
                    fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);
                }
                if(fileBrowser.webBrowserDisplay != null && fileBrowser.webBrowserDisplay.isVisible()) {
                    collectionStr = "";
                    startPointer = 0;
                    endPointer = 0;
                    fileBrowser.changeIcon(fileBrowser.webBrowserDisplay.steerImgs[4], "browserIcons", "open", "closed");
                }

                fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
            }
        });

        KeyboardIconLin.addView(keyboardicon);


        return KeyboardIconLin;
    }


    public void createKeyboard(ArrayList<String[]> arrayList) {

        GridLayout keyboardGrid = new GridLayout(fileBrowser);
        keyboardGrid.setLayoutParams(new RelativeLayout.LayoutParams(fileBrowser.displayWidth -10,fileBrowser.displayHeight/3 + fileBrowser.displayHeight/22));
        keyboardGrid.setRowCount(arrayList.size());
        keyboardGrid.setColumnCount(3);
        keyboardGrid.setUseDefaultMargins(true);
        //keyboardGrid.setBackgroundColor(getResources().getColor(R.color.black_overlay));
        keyboardGrid.setY(displayHeight/22);

        RelativeLayout.LayoutParams[] tabsLinParams = new RelativeLayout.LayoutParams[] {
                new RelativeLayout.LayoutParams((int)(displayWidth/14), (int)(displayWidth/14)),
                new RelativeLayout.LayoutParams((int)(displayWidth/14), (int)(displayWidth/14)),
                new RelativeLayout.LayoutParams((int)(displayWidth/12), (int)(displayWidth/14)),
                new RelativeLayout.LayoutParams((int)(displayWidth/12), (int)(displayWidth/12)),
                new RelativeLayout.LayoutParams((int)(displayWidth/3) -20, (int)(displayWidth/14)),
                new RelativeLayout.LayoutParams((int)(displayWidth/12), (int)(displayWidth/12)),
                new RelativeLayout.LayoutParams((int)(displayWidth/14), (int)(2*displayWidth/14))};
        for(int i=0;i<tabsLinParams.length;i++)
            tabsLinParams[i].addRule(RelativeLayout.CENTER_IN_PARENT);

        LinearLayout[] tabsLin = new LinearLayout[0];
        RelativeLayout[] tabsRel = new RelativeLayout[0];
        ImageView[] tabsButtons = new ImageView[0];
        TextView[] tabsTx = new TextView[0];
        TextView[] tabsTxShadow = new TextView[0];

        for(int i=0;i<arrayList.size(); i++) {
            for(int n=0;n<3;n++) {
                tabsLin = Arrays.copyOf(tabsLin, tabsLin.length + 1);
                tabsLin[tabsLin.length - 1] = new LinearLayout(fileBrowser);
                tabsLin[tabsLin.length - 1].setX(-5);
            }

            for (int i1=0;i1<arrayList.get(i).length; i1++) {
                tabsRel = Arrays.copyOf(tabsRel, tabsRel.length +1);
                tabsRel[tabsRel.length -1] = new RelativeLayout(fileBrowser);
                tabsRel[tabsRel.length -1].setTag(tabsRel.length -1);
                tabsRel[tabsRel.length -1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView txview = new TextView(fileBrowser);
                        for(int i=0;i<((RelativeLayout) v).getChildCount();i++) {
                            if((((float) i/2)+"").endsWith(".5")){
                                txview = ((TextView) ((RelativeLayout) v).getChildAt(i));
                                onClickHandling(txview);
                            }
                        }
                    }
                });

                tabsButtons = Arrays.copyOf(tabsButtons, tabsButtons.length +1);
                tabsButtons[tabsButtons.length -1] = new ImageView(fileBrowser);
                tabsTx = Arrays.copyOf(tabsTx, tabsTx.length +1);
                tabsTx[tabsTx.length -1] = new TextView(fileBrowser);
                tabsTx[tabsTx.length -1].setTextSize((float)(1.1*textSize));
                tabsTx[tabsTx.length -1].setTextColor(getResources().getColor(R.color.white));
                tabsTx[tabsTx.length -1].setText(arrayList.get(i)[i1]);
                tabsTx[tabsTx.length -1].setGravity(Gravity.CENTER);

                tabsTxShadow = Arrays.copyOf(tabsTxShadow, tabsTxShadow.length +1);
                tabsTxShadow[tabsTxShadow.length -1] = new TextView(fileBrowser);
                tabsTxShadow[tabsTxShadow.length -1].setTextSize((float)(1.1*textSize));
                tabsTxShadow[tabsTxShadow.length -1].setTextColor(getResources().getColor(R.color.grey));
                tabsTxShadow[tabsTxShadow.length -1].setText(arrayList.get(i)[i1]);
                tabsTxShadow[tabsTxShadow.length -1].setGravity(Gravity.CENTER);
                tabsTxShadow[tabsTxShadow.length -1].setX(2);
                tabsTxShadow[tabsTxShadow.length -1].setY(2);


                switch(arrayList.get(i)[i1].length()) {
                    case(1): {
                        int l = 0;
                        tabsRel[tabsRel.length -1].setLayoutParams(tabsLinParams[l]);
                        tabsButtons[tabsButtons.length -1].setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/letter.png"));
                        tabsButtons[tabsButtons.length -1].setLayoutParams(tabsLinParams[l]);
                        tabsTx[tabsTx.length -1].setLayoutParams(tabsLinParams[l]);
                        tabsTxShadow[tabsTxShadow.length -1].setLayoutParams(tabsLinParams[l]);

                        tabsRel[tabsRel.length -1].addView(tabsButtons[tabsButtons.length -1]);
                        tabsRel[tabsRel.length -1].addView(tabsTxShadow[tabsTxShadow.length -1]);
                        tabsRel[tabsRel.length -1].addView(tabsTx[tabsTx.length -1]);
                        break;
                    }
                    case(2): case(3): {
                        int l = 2;
                        tabsRel[tabsRel.length -1].setLayoutParams(tabsLinParams[1]);
                        tabsButtons[tabsButtons.length -1].setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/letter1.png"));
                        tabsButtons[tabsButtons.length -1].setLayoutParams(tabsLinParams[1]);
                        tabsTx[tabsTx.length -1].setLayoutParams(tabsLinParams[l]);
                        tabsTxShadow[tabsTxShadow.length -1].setLayoutParams(tabsLinParams[l]);

                        tabsRel[tabsRel.length -1].addView(tabsButtons[tabsButtons.length -1]);
                        tabsRel[tabsRel.length -1].addView(tabsTxShadow[tabsTxShadow.length -1]);
                        tabsRel[tabsRel.length -1].addView(tabsTx[tabsTx.length -1]);
                        break;
                    }
                    case(4): case(5): {
                        int l=2;
                        tabsRel[tabsRel.length -1].setLayoutParams(tabsLinParams[2]);
                        tabsButtons[tabsButtons.length -1].setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/letter1.png"));
                        tabsButtons[tabsButtons.length -1].setLayoutParams(tabsLinParams[l]);
                        tabsTx[tabsTx.length -1].setLayoutParams(tabsLinParams[l]);
                        tabsTxShadow[tabsTxShadow.length -1].setLayoutParams(tabsLinParams[l]);

                        if(arrayList.get(i)[i1].contains("Enter")) {
                            l=3;
                            tabsRel[tabsRel.length -1].setLayoutParams(tabsLinParams[l]);
                            tabsButtons[tabsButtons.length - 1].setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/letter2.png"));
                            tabsButtons[tabsButtons.length -1].setLayoutParams(tabsLinParams[l]);
                            tabsTx[tabsTx.length -1].setLayoutParams(tabsLinParams[l]);
                            tabsTx[tabsTx.length -1].setY(-keyBoardLayout.getHeight()/32 +15);
                            tabsTxShadow[tabsTxShadow.length -1].setLayoutParams(tabsLinParams[l]);
                            tabsTxShadow[tabsTxShadow.length -1].setY(-keyBoardLayout.getHeight()/32 +15);

                        }
                        if(arrayList.get(i)[i1].contains("Shift")) {
                            l=5;
                            tabsRel[tabsRel.length -1].setLayoutParams(tabsLinParams[l]);
                            tabsButtons[tabsButtons.length - 1].setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/letter1_1_closed.png"));
                            if(shift)
                                tabsButtons[tabsButtons.length - 1].setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/letter1_1_open.png"));
                            tabsButtons[tabsButtons.length -1].setLayoutParams(tabsLinParams[l]);
                            tabsTx[tabsTx.length -1].setLayoutParams(tabsLinParams[l]);
                            tabsTxShadow[tabsTxShadow.length -1].setLayoutParams(tabsLinParams[l]);

                        }
                        tabsRel[tabsRel.length -1].addView(tabsButtons[tabsButtons.length -1]);
                        tabsRel[tabsRel.length -1].addView(tabsTxShadow[tabsTxShadow.length -1]);
                        tabsRel[tabsRel.length -1].addView(tabsTx[tabsTx.length -1]);
                        break;
                    }
                    case(7): {
                        int l=4;
                        tabsRel[tabsRel.length -1].setLayoutParams(tabsLinParams[l]);
                        tabsButtons[tabsButtons.length -1].setImageBitmap(fileBrowser.bitmapLoader("KeyBoard/letter3.png"));
                        tabsButtons[tabsButtons.length -1].setLayoutParams(tabsLinParams[l]);
                        tabsTx[tabsTx.length -1].setLayoutParams(tabsLinParams[l]);
                        tabsTxShadow[tabsTxShadow.length -1].setLayoutParams(tabsLinParams[l]);

                        tabsRel[tabsRel.length -1].addView(tabsButtons[tabsButtons.length -1]);
                        tabsRel[tabsRel.length -1].addView(tabsTxShadow[tabsTxShadow.length -1]);
                        tabsRel[tabsRel.length -1].addView(tabsTx[tabsTx.length -1]);
                        break;
                    }
                }

                if(i1 == 0)
                    tabsLin[tabsLin.length - 3].addView(tabsRel[tabsRel.length - 1]);
                else if(i1 == arrayList.get(i).length -1)
                    tabsLin[tabsLin.length - 1].addView(tabsRel[tabsRel.length - 1]);
                else
                    tabsLin[tabsLin.length - 2].addView(tabsRel[tabsRel.length - 1]);

            }
            for(int n1=3;n1>0;n1--) {
                keyboardGrid.addView(tabsLin[tabsLin.length - n1]);
            }
        }

        keyBoardMainRel.addView(keyboardGrid);
    }

    private void onClickHandling(TextView view) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
            fileBrowser.vibrator.vibrate(vibrationEffect);
        }
        try {
            txEd = (EditText) fileBrowser.getCurrentFocus(); //fileBrowser.keyboardTrans;
        } catch (Exception e) {
            if(calledBack.equals("WebView")) {
                txEd = new EditText(fileBrowser);
                txEd.setText(collectionStr);
                txEd.setSelection(startPointer);
                if(endPointer > startPointer)
                    txEd.setSelection(startPointer,endPointer);

            } else {
                try {
                    txEd = fileBrowser.keyboardTrans;
                } catch (NullPointerException ne) {
                    fileBrowser.messageStarter("noEditFildSelected", docu_Loader("Language/" + language + "/NoEditFildSelected.txt"), 7000);
                    return;
                }
            }
        }

        String tab = view.getText().toString();

        if(txEd != null) {

            startPointer = txEd.getSelectionStart();
            endPointer = txEd.getSelectionEnd();
            if(endPointer <= startPointer)
                endPointer = startPointer;

            if(tab.length() == 1) {
                if(calledFrom.equals("pdfTx"))
                    txEd.setLayoutParams(new RelativeLayout.LayoutParams(txEd.getWidth()+2*textSize,txEd.getHeight()));

                txEd.setText(txEd.getText().toString().substring(0,startPointer) + tab + txEd.getText().toString().substring(endPointer));
                txEd.setSelection(startPointer + tab.length());
                startPointer = startPointer + tab.length();
                endPointer = startPointer;
            } else
                tab = tab.trim();

            if(tab.length() == 3 && tab.contains(" ")) {
                String ch = tab.substring(0,1);

                txEd.setText(txEd.getText().toString().substring(0,startPointer) + ch + txEd.getText().toString().substring(endPointer));
                txEd.setSelection(startPointer + ch.length());
                startPointer = startPointer + ch.length();
                endPointer = startPointer;
            }

            switch (tab) {
                case("Enter"): {
                    txEd.setText(txEd.getText().toString().substring(0,startPointer) + "\n" + txEd.getText().toString().substring(endPointer));
                    startPointer = startPointer +1;
                    txEd.setSelection(startPointer);
                    endPointer = startPointer;
                    break;
                }
                case("Back"): {
                    if(calledFrom.equals("pdfTx"))
                        txEd.setLayoutParams(new RelativeLayout.LayoutParams(txEd.getWidth()-2*textSize,txEd.getHeight()));

                    if(txEd.getText().toString().length() >0) {
                        startPointer = txEd.getSelectionStart() -1;
                        if(startPointer < 0) startPointer = 0;
                        endPointer = txEd.getText().toString().length();
                        txEd.setText(txEd.getText().toString().substring(0,startPointer) + txEd.getText().toString().substring(startPointer +1, endPointer));
                        endPointer = startPointer;
                        txEd.setSelection(startPointer);
                    }
                    break;
                }
                case("Tab"): {
                    txEd.setText(txEd.getText().toString().substring(0,startPointer) + "          " + txEd.getText().toString().substring(endPointer));
                    txEd.setSelection(startPointer + tab.length());
                    startPointer = startPointer + tab.length();
                    endPointer = startPointer;
                    txEd.setSelection(startPointer);
                    break;
                }
                case("{ }"): {
                    if(calledFrom.equals("pdfTx"))
                        txEd.setLayoutParams(new RelativeLayout.LayoutParams(txEd.getWidth()+4*textSize,txEd.getHeight()));

                    txEd.setText(txEd.getText().toString().substring(0,startPointer) + tab + txEd.getText().toString().substring(endPointer));
                    txEd.setSelection(startPointer + tab.length());
                    startPointer = startPointer + tab.length();
                    endPointer = startPointer;
                    break;
                }
                case("VVVV"): {
                    shift = true; shiftFixed = true;
                    keyBoardMainRel.removeAllViews();
                    kindOf_keys = "large";
                    createKeyboard(largeTabsTx);
                    if(!calledBack.equals("WebView"))
                        keyBoardMainRel.addView(createKeyboardIcon());
                    break;
                }
                case("vvvv"): {
                    shift = false; shiftFixed = false;
                    keyBoardMainRel.removeAllViews();
                    kindOf_keys = "small";
                    createKeyboard(smallTabsTx);
                    if(!calledBack.equals("WebView"))
                        keyBoardMainRel.addView(createKeyboardIcon());
                    break;
                }
                case("vv"): {
                    String tx = txEd.getText().toString();
                    startPointer = txEd.getSelectionStart();
                    String[] txLines = tx.split("\n");
                    int  i = 0,
                            i1 = 0;
                    for(i=0;i<txLines.length; i++) {
                        i1 = i1 + txLines[i].length() +1;
                        if(i1 >= startPointer && i < txLines.length -1) {
                            txEd.setSelection(i1+txLines[i +1].length());
                            break;
                        }
                    }

                    startPointer = txEd.getSelectionStart();
                    break;
                }
                case("^^"): {
                    String tx = txEd.getText().toString();
                    startPointer = txEd.getSelectionStart();
                    String[] txLines = tx.split("\n");
                    int  i = 0,
                            i1 = 0;
                    for(i=0;i<txLines.length; i++) {
                        i1 = i1 + txLines[i].length() +1;
                        if(i1 >= startPointer && i > 0) {
                            txEd.setSelection(i1 -txLines[i].length() -2);
                            break;
                        }
                    }

                    startPointer = txEd.getSelectionStart();
                    break;
                }
                case("Shift"): {
                    if(shift) {
                        shift = false;
                        keyBoardMainRel.removeAllViews();
                        kindOf_keys = "small";
                        createKeyboard(smallTabsTx);
                        keyBoardMainRel.addView(createKeyboardIcon());
                    } else {
                        shift = true;
                        keyBoardMainRel.removeAllViews();
                        kindOf_keys = "large";
                        createKeyboard(largeTabsTx);
                        keyBoardMainRel.addView(createKeyboardIcon());
                    }

                    break;
                }
                case("Clear"): {
                    if(txEd.getSelectionStart() == txEd.getSelectionEnd()) {
                        txEd.setText("");
                        txEd.setSelection(0);
                        startPointer = 0;
                        endPointer = 0;
                    } else {
                        startPointer = txEd.getSelectionStart();
                        endPointer = txEd.getSelectionEnd();
                        txEd.setText(txEd.getText().toString().substring(0, startPointer) + txEd.getText().toString().substring(endPointer));
                        txEd.setSelection(startPointer);
                        endPointer = startPointer;
                    }
                    break;
                }
                case("Copy"): {
                    new keyblinkIcon(view, "COPY").start();
                    String st = txEd.getText().toString();
                    if(startPointer != endPointer)
                        st = txEd.getText().toString().substring(startPointer, endPointer);
                    clipBoard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", st);
                    clipBoard.setPrimaryClip(clip);
                    break;
                }
                case("Paste"): {
                    new keyblinkIcon(view, "PASTE").start();
                    ClipData.Item item;
                    if(clipBoard.getPrimaryClip() != null) {
                        item = clipBoard.getPrimaryClip().getItemAt(0);
                        if(calledFrom.equals("pdfTx")) {
                            
                            int rowheight, rowwidth;

                            float  rowh = item.getText().toString().length()/55;
                            rowwidth = displayWidth -displayWidth/6;
                            if ((rowh+"").startsWith("0.")) {
                                rowh = 1;
                                rowwidth = 2*textSize * item.getText().toString().length();
                            }
                            txEd.setBackgroundColor(getResources().getColor(R.color.white_overlay));
                            rowheight = (int)(txEd.getHeight() * rowh);

                            txEd.setLayoutParams(new RelativeLayout.LayoutParams(rowwidth, rowheight));
                            txEd.setText(txEd.getText().toString().substring(0, startPointer) + item.getText().toString() + txEd.getText().toString().substring(endPointer));
                            txEd.setSelection(txEd.getText().toString().length());
                            break;
                        }
                        txEd.setText(txEd.getText().toString().substring(0, startPointer) + item.getText().toString() + txEd.getText().toString().substring(endPointer));
                        txEd.setSelection(endPointer);
                        startPointer = endPointer;
                    }
                    break;
                }
                case("TAB"): {
                    if(calledFrom.equals("pdfTx"))
                        txEd.setLayoutParams(new RelativeLayout.LayoutParams(txEd.getWidth()+2*textSize,txEd.getHeight()));

                    txEd.setText(txEd.getText().toString().substring(0,startPointer) + " " + txEd.getText().toString().substring(endPointer));
                    startPointer = startPointer +1;
                    txEd.setSelection(startPointer);
                    endPointer = startPointer;
                    break;
                }
                case(">"): case("<"): {
                    if(calledFrom.equals("pdfTx"))
                        txEd.setLayoutParams(new RelativeLayout.LayoutParams(txEd.getWidth()+2*textSize,txEd.getHeight()));

                    txEd.setText(txEd.getText().toString().substring(0,startPointer) + tab + txEd.getText().toString().substring(endPointer));
                    txEd.setSelection(startPointer + tab.length());
                    startPointer = startPointer + tab.length();
                    endPointer = startPointer;
                    break;
                }
                case(">>"): {
                    if(txEd.getSelectionStart() < txEd.getText().toString().length()) {
                        startPointer = txEd.getSelectionStart() +1;
                        txEd.setSelection(startPointer);
                        endPointer = startPointer;
                    }
                    break;
                }
                case("<<"): {
                    if(txEd.getSelectionStart() > 0) {
                        startPointer = txEd.getSelectionStart() -1;
                        txEd.setSelection(startPointer);
                        endPointer = startPointer;
                    }
                    break;
                }
            }

            if((!tab.equals("Shift") && shift && !shiftFixed) || (tab.equals("Shift") && shiftFixed))  {
                shift = false;
                keyBoardMainRel.removeAllViews();
                kindOf_keys = "small";
                createKeyboard(smallTabsTx);
                keyBoardMainRel.addView(createKeyboardIcon());
            } else if(!tab.equals("Shift") && !shift && shiftFixed) {
                shift = true;
                keyBoardMainRel.removeAllViews();
                kindOf_keys = "large";
                createKeyboard(largeTabsTx);
                keyBoardMainRel.addView(createKeyboardIcon());
            }
            if(calledBack.equals("WebView") && fileBrowser.webBrowserDisplay != null && fileBrowser.webBrowserDisplay.isVisible()) {
                collectionStr = txEd.getText().toString();
                fileBrowser.webBrowserDisplay.handleJavascriptInput(collectionStr,startPointer,endPointer);
            }
        }
    }
    static public class keyblinkIcon extends Thread {
        View iView;
        boolean run = true;
        String kindOf = "";

        public keyblinkIcon(View v, String kind) {
            iView = v;
            kindOf = kind;
        }

        public void run() {
            String url = "";
            iView.setEnabled(false);
            int n = 0;
            while (run) {
                if(kindOf.equals("OK"))
                    url = "KeyBoard/letterOK";
                else if(kindOf.equals("COPY"))
                    url = "KeyBoard/letter1";
                else if(kindOf.equals("PASTE"))
                    url = "KeyBoard/letter1";
                else if(kindOf.equals("search"))
                    url = "KeyBoard/search";


                try {
                    Thread.sleep(250);
                } catch (InterruptedException ie) {
                }
                if(kindOf.equals("OK") || kindOf.equals("search")) {
                    final String finalUrl = url;
                    fileBrowser.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (iView.getTag().toString().contains("closed")) {
                                iView.setTag(iView.getTag().toString().replace("closed", "open"));
                                ((ImageView) iView).setImageBitmap(fileBrowser.bitmapLoader(finalUrl + "_open.png"));
                            } else {
                                iView.setTag(iView.getTag().toString().replace("open", "closed"));
                                ((ImageView) iView).setImageBitmap(fileBrowser.bitmapLoader(finalUrl + "_closed.png"));
                            }
                        }
                    });

                } else {
                    TextView txView = (TextView) iView;
                    if(txView.getCurrentTextColor() == fileBrowser.getResources().getColor(R.color.white))
                        txView.setTextColor(fileBrowser.getResources().getColor(R.color.blue));
                    else
                        txView.setTextColor(fileBrowser.getResources().getColor(R.color.white));
                }

                if(n >= 20) {
                    run = false;
                    break;
                }
                n++;
            }
            if(!kindOf.equals("OK") && !kindOf.equals("search"))
                ((TextView)iView).setTextColor(fileBrowser.getResources().getColor(R.color.black));
            else {
                String finalUrl1 = url;
                fileBrowser.runOnUiThread(new Runnable() {
                    final String finalUrl = finalUrl1;
                    @Override
                    public void run() {
                        if (iView.getTag().toString().contains("open")) {
                            ((ImageView) iView).setImageBitmap(fileBrowser.bitmapLoader(finalUrl + "_closed.png"));
                        }
                    }
                });
            }
        }
    }
}