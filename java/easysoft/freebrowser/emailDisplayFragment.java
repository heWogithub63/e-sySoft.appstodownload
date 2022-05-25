package easysoft.freebrowser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import android.widget.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import static easysoft.freebrowser.FileBrowser.*;

public class emailDisplayFragment extends Fragment {

    View view;
    FrameLayout emailLayout;
    ImageView selector;
    float previousX;
    RelativeLayout mainRel;
    LinearLayout mainLin;
    LinearLayout TextLin;
    LinearLayout AttachLin;
    LinearLayout header;
    ArrayList<String[]> attachedList;
    ArrayList<String[]> collectionMemoryList;
    String[] memoryList, sd;
    HorizontalScrollView headEMScroll;

    String emailAddress = "";
    
    static boolean attachment = false, mailAttached = false;
    static ImageView[] icons;
    static ImageView toChoose;
    static EditText[] headerEdit;
    static TextView[] folderNames;
    static String[] mailAccountData = new String[0];
    static String[] praefix = new String[0];
    static EditText mailTx;
    static String posIcon = "", folderName ="";
    static String deleteIndividium ="";
    static int n1 = 0, nn = 0;
    ScrollView folderInxScr;

    static boolean createMail = false;
    boolean textIsHtml = false;

    public emailDisplayFragment() {
    }


    public static emailDisplayFragment newInstance() {
        emailDisplayFragment fragment = new emailDisplayFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            emailAddress = getArguments().getString("EMAILADD");
            fileBrowser.webBrowserDisplay.webView.loadUrl(fileBrowser.webBrowserDisplay.urlCollection.get(fileBrowser.webBrowserDisplay.urlCollectionCounter));
        }
        memoryList = new String[]{"","","",""};
        mailAccountData = fileBrowser.read_writeFileOnInternalStorage("read", "AccountData", "MailAccountData.txt", "");
        if(mailAccountData != null && mailAccountData.length > 0) {

            int i = 0;
            for(i=0; i< mailAccountData.length; i++)
                if(mailAccountData[i].contains("(!"))
                    break;
            if(i > 9)
               n1 = ((mailAccountData.length - 2) / 7) -1;
            else
                n1 = 1;
        }
        emailLayout = fileBrowser.frameLy.get(5);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_email_display, container, false);
        mainRel = (RelativeLayout) view.findViewById(R.id.mainRel);

        mainRel.addView(createSendEmailDisplay(createHaederMail()));
        mainRel.addView((createSwitcher()));

        emailLayout.bringToFront();
        return view;
    }

    private RelativeLayout createSwitcher() {
        calledFrom = "";
        RelativeLayout header = new RelativeLayout(fileBrowser);
        header.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth, displayHeight/12));
        header.setY(displayHeight -displayHeight/8);

        header.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                float newX = 0;

                switch (e.getAction()) {
                    case (MotionEvent.ACTION_DOWN): {
                        previousX = e.getX();
                    }
                    case (MotionEvent.ACTION_UP): {
                        newX = e.getX();
                    }
                }

                if ((previousX - newX) < -100) {
                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                        icons[0].setTag(icons[0].getTag().toString().replace("open", "closed"));
                        icons[0].setImageBitmap(fileBrowser.bitmapLoader("Icons/mailIcons/" + icons[0].getTag().toString().substring(
                                icons[0].getTag().toString().indexOf(" ") +1)));
                        arrayList = new ArrayList<>();
                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                    }
                    if (fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible()) {
                        fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard, 6);
                    }
                    fileBrowser.startMovePanel(5);
                }
                return true;
            }
        });
        return header;
    }

    public void startMailSend() {
        new HandleMailWithAttachmen().execute();
    }

    public void createNewDisplay() {
        mainRel.removeAllViews();
        mainRel.addView(createSendEmailDisplay(createHaederMail()));
        mainRel.addView(createSwitcher());
        fileBrowser.threadStop = true;
    }

    public void showFolderIndex(String tag) {

            folderName = tag;
            folderNames = new TextView[0];
            sd = fileBrowser.read_writeFileOnInternalStorage("read", folderName, "", "");
            Arrays.sort(sd);
            folderInxScr = new ScrollView(fileBrowser);
            folderInxScr.setLayoutParams(new RelativeLayout.LayoutParams((int)(5*fileBrowser.displayWidth /7), (int)(2*displayHeight/5)));
            folderInxScr.setX(fileBrowser.displayWidth /7);
            folderInxScr.setY(2*displayHeight/5);
            LinearLayout folderInx = new LinearLayout(fileBrowser);
            folderInx.setOrientation(LinearLayout.VERTICAL);
            folderInx.setPadding(15,15,15,15);
            folderInx.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

            int txSize = 3;
            if(yfact < 0.625)
                txSize = 4;
            for (String s : sd) {
                if (!s.startsWith(".")) {
                    folderNames = Arrays.copyOf(folderNames, folderNames.length + 1);
                    folderNames[folderNames.length - 1] = new TextView(fileBrowser);
                    folderNames[folderNames.length - 1].setText(s);
                    folderNames[folderNames.length - 1].setTextSize((int) (textSize + txSize));
                    folderNames[folderNames.length - 1].setTag("- " + s + "_closed");
                    folderNames[folderNames.length - 1].setTextColor(getResources().getColor(R.color.black));
                    folderNames[folderNames.length - 1].setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            String add = "";
                            String tag = ((TextView) v).getText().toString();
                            String[] trans = fileBrowser.read_writeFileOnInternalStorage("read", folderName, tag, "");
                            praefix = fileBrowser.docu_Loader("Language/" + language + "/MailHeadLines.txt");

                            memoryList = new String[4];
                            createMail = false;
                            boolean mm = false;

                            boolean attached = false;
                            int n = 0;

                            for (String s : trans) {
                                if (n < 3) {
                                    if (n == 0 && folderName.startsWith("Mail") && s.contains(" -> ")) {
                                        add = s.substring(s.indexOf(" -> ") + 4);
                                    }
                                    if (!folderName.startsWith("In"))
                                        s = praefix[n] + " " + s;
                                    memoryList[n] = s;
                                } else if (n >= 3 && !(mm || attached) && !s.startsWith("Attached")) {
                                    if (n == 3)
                                        memoryList[3] = "\n";
                                    memoryList[3] = memoryList[3] + s + "\n";
                                } else if (s.startsWith("Attached") && !folderName.startsWith("In")) {
                                    mm = true;
                                    memoryList[3] = memoryList[3] + s + "\n";
                                } else if (!folderName.startsWith("In")) {
                                    memoryList[3] = memoryList[3] + s.substring(s.lastIndexOf("/") + 1) + "\n\t";
                                } else if (!attached && folderName.startsWith("In")) {
                                    attachedList = new ArrayList<>();
                                    attached = true;
                                } else if (attached && folderName.startsWith("In")) {
                                    attachedList.add(new String[]{s.substring(0, s.lastIndexOf("/")), s.substring(s.lastIndexOf("/") + 1)});
                                }

                                n++;
                            }
                            if (folderName.startsWith("In")) {
                                posIcon = "New_closed.png";
                                createMail = true;
                                if (fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                    arrayList = new ArrayList<>();
                                    fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                    fileBrowser.changeIcon(icons[0], "mailIcons", "open", "closed");
                                }
                            } else {
                                String mem = "";
                                for (String s : memoryList) {
                                    mem = mem + s + "\n";
                                }
                                memoryList[3] = mem;
                            }

                            if(folderName.startsWith("Mail")) {
                                attachedList = new ArrayList<>();
                                String folder = folderName.replace(" ","").trim();
                                String[] tmp = new File("/storage/self/primary/tmp/"+folder).list();
                                String subject = memoryList[2].replace("'", "").replace(".", "").replace(" ", "")
                                        .replace("’", "").replace(",","").replace("(","")
                                        .replace(")","").replace("?","").replace("/","").trim();
                                        subject = subject.substring(subject.indexOf(":") +1);
                                for(String s: tmp) {
                                    if (s.contains("AttachedFile")) {
                                        if (s.contains(subject)) {
                                            attachedList.add(new String[]{"/storage/self/primary/tmp/" +
                                                    folder + "/" + s, s.substring(1)});
                                            mailAttached = true;
                                        }
                                    }
                                }
                            }

                            saveAddressant(add);
                            createNewDisplay();
                            if(!folderName.contains("In"))
                               createMailBack();
                            return true;
                        }
                    });
                    folderNames[folderNames.length - 1].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String tag = ((TextView) v).getText().toString();
                            if (v.getTag().toString().contains("closed")) {
                                fileBrowser.changeIcon(v, "", "closed", "open");
                                deleteIndividium = folderName + ":" + tag;
                            } else {
                                fileBrowser.changeIcon(v, "", "open", "closed");
                                deleteIndividium = "";
                            }
                        }
                    });
                    folderInx.addView(folderNames[folderNames.length - 1]);
                }

                folderInxScr.removeAllViews();
                mainRel.removeView(folderInxScr);

                folderInxScr.addView(folderInx);
                mainLin.removeView(TextLin);
                mainRel.addView(folderInxScr);

            }
        fileBrowser.threadStop = true;
    }

    private void refreshMemoryList() {
        memoryList = new String[0];
        for(int hS=0; hS<headerEdit.length; hS++) {
            memoryList = Arrays.copyOf(memoryList, memoryList.length +1);
            memoryList[memoryList.length -1]=headerEdit[hS].getText().toString();
        }
        memoryList = Arrays.copyOf(memoryList, memoryList.length +1);
        memoryList[memoryList.length -1] = mailTx.getText().toString();

    }

    public void saveEmail(String folderName) {
        String mailFrom = "", subject = "";
        for(int i=0;i<collectionMemoryList.size(); i++) {
            StringBuffer MailsInCreation = new StringBuffer();
            for (int i1 = 0; i1 < collectionMemoryList.get(i).length; i1++) {
                if (i1 == 0) {
                    mailFrom = collectionMemoryList.get(i)[i1];
                }
                if (i1 == 2)
                    subject = collectionMemoryList.get(i)[i1].replace("/","°");

                if (i1 == 3 && (folderName.startsWith("Mail") && (textIsHtml ||
                        collectionMemoryList.get(i)[i1].contains("HTML") || collectionMemoryList.get(i)[i1].contains("html") ) ||
                        collectionMemoryList.get(i)[i1].contains("MimeMultipart@"))) {
                    String kind = "";
                    if (textIsHtml || collectionMemoryList.get(i)[i1].contains("HTML") || collectionMemoryList.get(i)[i1].contains("html"))
                        kind = "Attached_HTML.html";
                    else if (collectionMemoryList.get(i)[i1].contains("MimeMultipart@"))
                        kind = "Attached_MimeMulty.html";

                    String sub = subject.replace("'", "").replace(".", "").replace(" ", "")
                            .replace("’", "").replace(",","").replace("(","")
                            .replace(")","").replace("?","").replace("/","").trim(),
                            fold = folderName.replace(" ", "");

                    if (fileBrowser.read_writeFileOnInternalStorage("write", "/storage/self/primary/tmp/" + fold, "." + sub + "_" + kind, collectionMemoryList.get(i)[i1]).length == 0)
                        collectionMemoryList.get(i)[i1] = kind;

                }

                MailsInCreation.append(collectionMemoryList.get(i)[i1] + "\n");

            }
            MailsInCreation.append("Attached -->\n");
            
            if(!new File(fileBrowser.context.getFilesDir() + folderName,mailFrom+subject).isFile())
                if(fileBrowser.read_writeFileOnInternalStorage("write", folderName, mailFrom +
                        " " +subject, MailsInCreation.toString()).length == 0)
                    continue;

        }
        if(folderName.contains("Sent") || folderName.contains("Gesendete")) {
            fileBrowser.messageStarter("mailSaved", docu_Loader("Language/" + language + "/MailSent.txt"),  5000);
        } else if(folderName.contains("Mail Eingang") || folderName.contains("Mails Arrived")) {
            fileBrowser.messageStarter("mailSaved", docu_Loader("Language/" + language + "/MailArrived.txt"), 5000);
        } else {
            fileBrowser.messageStarter("mailSaved", docu_Loader("Language/" + language + "/MailSaved.txt"),  5000);
            fileBrowser.changeIcon(icons[5], "mailIcons", "closed","open");
        }
    }

    public boolean saveAddressant (String add) {

        String[]  emailAddr = fileBrowser.read_writeFileOnInternalStorage("read", "ToAddresses","emailAddresses.txt", ""),
                emailAddr1 = new String[0];
        String emailAddrTrans = "";

        for (int i=0; i<emailAddr.length;i++) {
            if (!emailAddr[i].equals(add) && !emailAddr[i].equals("")) {
                emailAddr1 = Arrays.copyOf(emailAddr1, emailAddr1.length +1);
                emailAddr1[emailAddr1.length -1] = emailAddr[i];
            }
        }
        emailAddr = emailAddr1;
        emailAddr = Arrays.copyOf(emailAddr, emailAddr.length + 1);
        emailAddr[emailAddr.length - 1] = add;

        Arrays.sort(emailAddr);

        for(String s : emailAddr)
            emailAddrTrans = emailAddrTrans +s+ "\n";

        fileBrowser.read_writeFileOnInternalStorage("write", "ToAddresses","emailAddresses.txt", emailAddrTrans);

        return true;
    }

    public LinearLayout createSendEmailDisplay(LinearLayout mainLin) {
        header = new LinearLayout(fileBrowser);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout[] headerLin = new LinearLayout[0];
        String[] headerTx = new String[]{"FromLin", "ToLin", "RegardLin"};
        headerEdit = new EditText[0];

        TextLin = new LinearLayout(fileBrowser);
        TextLin.setLayoutParams(new RelativeLayout.LayoutParams(emailLayout.getWidth() - 30,  emailLayout.getHeight() / 2));
        TextLin.setOrientation(LinearLayout.HORIZONTAL);

        if(!createMail && !mailAttached) {
            TextLin.setLayoutParams(new RelativeLayout.LayoutParams(emailLayout.getWidth() - 30,  (int)(emailLayout.getHeight() / 2) +
                    emailLayout.getHeight() / 4));
            TextLin.setPadding(10, (int) (emailLayout.getHeight() / 4 +20), 10, 10);
        }
        if(mailAttached) {
            TextLin.setLayoutParams(new RelativeLayout.LayoutParams(emailLayout.getWidth() - 30,  2*emailLayout.getHeight() / 3));
            TextLin.setPadding(10, (int) (emailLayout.getHeight() / 4 + 20), 10, 10);
        }

        AttachLin = new LinearLayout(fileBrowser);
        AttachLin.setLayoutParams(new RelativeLayout.LayoutParams(new RelativeLayout.LayoutParams(displayWidth,displayHeight/5)));
        AttachLin.setOrientation(LinearLayout.HORIZONTAL);
        AttachLin.setPadding(20,0,20,0);

        if(posIcon.contains("New") || createMail || emailAddress.length() > 0)
            praefix = fileBrowser.docu_Loader("Language/"+language+"/MailHeadLines.txt");
        else
            praefix = new String[0];
        if (mailAccountData != null && mailAccountData.length > 0 && (createMail || emailAddress.length() > 0)) {
            for (int i = 0; i < headerTx.length; i++) {

                headerLin = Arrays.copyOf(headerLin, headerLin.length + 1);
                headerLin[headerLin.length - 1] = new LinearLayout(fileBrowser);
                headerLin[headerLin.length - 1].setLayoutParams(new RelativeLayout.LayoutParams(emailLayout.getWidth() - 30, emailLayout.getHeight() / 18));
                headerLin[headerLin.length - 1].setOrientation(LinearLayout.HORIZONTAL);

                TextView praeTx = new TextView(fileBrowser);
                praeTx.setLayoutParams(new RelativeLayout.LayoutParams((emailLayout.getWidth() / 4), emailLayout.getHeight() / 18));
                praeTx.setTextColor(getResources().getColor(R.color.black));
                praeTx.setTextSize(textSize);
                praeTx.setText("");
                if(praefix.length > 0)
                    praeTx.setText("\t" + praefix[i]);

                headerEdit = Arrays.copyOf(headerEdit, headerEdit.length + 1);
                headerEdit[headerEdit.length - 1] = new EditText(fileBrowser);
                headerEdit[headerEdit.length - 1].setLayoutParams(new RelativeLayout.LayoutParams((3 * emailLayout.getWidth() / 5), emailLayout.getHeight() / 22));
                headerEdit[headerEdit.length - 1].setTextColor(getResources().getColor(R.color.white));
                headerEdit[headerEdit.length - 1].setTextSize(textSize);
                headerEdit[headerEdit.length - 1].setTag(headerTx[i]);
                headerEdit[headerEdit.length - 1].setBackgroundColor(getResources().getColor(R.color.grey_overlay));
                headerEdit[headerEdit.length - 1].setPadding(10, 5, 10, 5);
                headerEdit[headerEdit.length - 1].setText(memoryList[i]);
                if(headerEdit.length -1 == 1 && emailAddress.length() > 1)
                    headerEdit[headerEdit.length - 1].setText(emailAddress);
                headerEdit[headerEdit.length - 1].setTag(i+"");
                headerEdit[headerEdit.length - 1].setShowSoftInputOnFocus(false);
                headerEdit[headerEdit.length - 1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        memoryList[Integer.parseInt(v.getTag().toString())] = "";
                        ((EditText)v).setText("");
                        fileBrowser.keyboardTrans = ((EditText) v);
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
                                    displayWidth -10, (int)(displayHeight/3) +fact01);}
                });
                if (i == 0 && (mailAccountData != null && mailAccountData.length > 0))
                    headerEdit[headerEdit.length - 1].setText(mailAccountData[6].substring(mailAccountData[6].indexOf(": ") + 2).trim());

                headerLin[headerLin.length - 1].addView(praeTx);
                headerLin[headerLin.length - 1].addView(headerEdit[headerEdit.length - 1]);

                if (i == 1) {
                    toChoose = new ImageView(fileBrowser);
                    toChoose.setLayoutParams(new RelativeLayout.LayoutParams((emailLayout.getHeight() / 18), emailLayout.getHeight() / 22));
                    toChoose.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/toChooser_closed.png"));
                    toChoose.setTag("_ toChooser_closed.png");
                    toChoose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(v.getTag().toString().contains("closed")) {
                                fileBrowser.changeIcon(v, "browserIcons", "closed", "open");

                                double f = 2;
                                if(yfact <= 0.625)
                                    f = 1.5;

                                int[] iconpos = new int[2];
                                v.getLocationOnScreen(iconpos);

                                fileBrowser.createList("mailAddList",1, "ToAddresses emailAddresses.txt",6,iconpos[0],
                                        (int)(iconpos[1] + v.getHeight()/2), (int)(displayWidth/f),"lu");
                                fileBrowser.frameLy.get(3).bringToFront();
                            } else {
                                deleteIndividium = "";
                                fileBrowser.changeIcon(v, "browserIcons", "open", "closed");
                                if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                    arrayList = new ArrayList<>();
                                    fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                }
                            }
                        }
                    });
                    headerLin[headerLin.length - 1].addView(toChoose);
                }

                header.addView(headerLin[headerLin.length - 1]);
            }
            mainLin.addView(header);
        }

        ScrollView txScroll = new ScrollView(fileBrowser);
        txScroll.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        LinearLayout mailTxLin = new LinearLayout(fileBrowser);
        mailTxLin.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mailTxLin.setOrientation(LinearLayout.VERTICAL);

        mailTx = new EditText(fileBrowser);
        mailTx.setTextColor(getResources().getColor(R.color.black));
        mailTx.setText(memoryList[3]);
        mailTx.setShowSoftInputOnFocus(false);
        mailTx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileBrowser.keyboardTrans = (EditText) v;
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
        });

        if (mailTx.getText().toString().contains("(!") && mailTx.getText().toString().contains("!)")) {
            selector = new ImageView(fileBrowser);
            selector.setLayoutParams(new RelativeLayout.LayoutParams((int) (80 * xfact), (int) (80 * xfact)));
            selector.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/txEditorSelector.png"));
            selector.setX(3 * displayWidth / 5);
            selector.setY(2 * displayHeight / 7);

            selector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mailTx.getText().toString().contains("(!") && mailTx.getText().toString().contains("!)")) {
                        mailTx.setSelection(mailTx.getText().toString().indexOf("(!"),
                                mailTx.getText().toString().indexOf("!)") + 2);
                    }
                }
            });

            fileBrowser.createSendEmail.mainRel.addView(selector);
        }

        if (praefix.length > 0 && (mailAccountData != null && mailAccountData.length > 0) && createMail &&
                !mailTx.getText().toString().contains(praefix[3])) {
            if (folderName.equals(""))
                mailTx.setText(praefix[3] + "\n\n" + mailTx.getText().toString());
        }


        mailTx.setTextSize(textSize + 3);
        mailTxLin.addView(mailTx);

        if(folderName.startsWith("Mail") && memoryList[3].contains("Attached_")) {

            String kind = "", kind_of = "", subject = "";
            String[] memoryStr = memoryList[3].split("\n");
            for (int i = 0; i < 3; i++)
                kind_of = kind_of + memoryStr[i] + "\n";
            mailTx.setText(kind_of);
            int l = 0;
            for(String s: memoryStr) {
                if (l==2)
                    subject = s.substring(s.indexOf(" ") + 1).replace(".", "").replace("'", "");
                if (s.contains("HTML") || s.contains("bodyPart")) {
                    kind = s.replace("null", "");
                }
                l++;
            }
            kind = subject.replace("'", "").replace(".", "").replace(" ", "")
                    .replace("’", "").replace(",","").replace("(","")
                    .replace(")","").replace("?","").replace("/","°").trim()
                    +"_"+kind;
            TextView mailTxLink = new TextView(fileBrowser);
            mailTxLink.setTextColor(getResources().getColor(R.color.blue_overlay));
            mailTxLink.setTextSize(textSize + 3);
            mailTxLink.setText(kind);
            mailTxLink.setTag(folderName+" _ "+kind);

            mailTxLink.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    /*int color = ((TextView)view).getCurrentTextColor();
                    if(color == getResources().getColor(R.color.black))
                        ((TextView)view).setTextColor(getResources().getColor(R.color.blue));
                    else
                        ((TextView)view).setTextColor(getResources().getColor(R.color.black));*/

                    String folder = view.getTag().toString().substring(0,view.getTag().toString().indexOf(" _ ")).replace(" ",""),
                            file = view.getTag().toString().substring(view.getTag().toString().indexOf(" _ ") +3).trim().replace("’", "").replace(" ", "");

                    if(file.endsWith(".html")) {
                        if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                            fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                        calledFrom = "email";
                        fileBrowser.startExtApp("file://" +"/storage/self/primary/tmp/" + folder + "/." +  file);
                    }

                    return true;
                }
            });

            mailTxLin.addView(mailTxLink);

        }

        txScroll.addView(mailTxLin);
        TextLin.addView(txScroll);

        if((attachedList != null && attachedList.size() > 0 && createMail) || mailAttached) {
            RelativeLayout.LayoutParams attachedLayParam = new RelativeLayout.LayoutParams((int) (220 * xfact), (int) (120 * xfact));
            RelativeLayout[] attRel = new RelativeLayout[0];

            for(int i=0; i<attachedList.size(); i++) {
                for (int i1 = 0; i1 < attachedList.get(i).length; i1++) {
                    if ((((float) i1 / 2) + "").endsWith(".5")) {
                        attRel = Arrays.copyOf(attRel, attRel.length + 1);
                        attRel[attRel.length - 1] = new RelativeLayout(fileBrowser);
                        attRel[attRel.length - 1].setLayoutParams(attachedLayParam);
                        attRel[attRel.length - 1].setX((float) (i * 5 * xfact));

                        TextView attachedTx = new TextView(fileBrowser);
                        attachedTx.setTextSize((int) (textSize));
                        attachedTx.setLayoutParams(new RelativeLayout.LayoutParams((int) (220 * xfact), (int) (80 * xfact)));
                        attachedTx.setTextColor(getResources().getColor(R.color.black));
                        attachedTx.setText(attachedList.get(i)[1]);
                        attachedTx.setTag(i +" "+attachedList.get(i)[0]+"_closed");
                        attachedTx.setY((float) (35 * xfact));
                        attachedTx.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(v.getTag().toString().contains("_closed")) {
                                    fileBrowser.changeIcon(v, "", "closed", "open");
                                    deleteIndividium = v.getTag().toString().substring(0, v.getTag().toString().indexOf(" "))+" attachedList";
                                }
                                else if(v.getTag().toString().contains("_open")) {
                                    fileBrowser.changeIcon(v, "", "open", "closed");
                                    deleteIndividium = "";
                                }
                            }
                        });
                        attachedTx.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                String tag = view.getTag().toString();

                                if(folderName.startsWith("Mail")) {
                                    calledFrom = "email";
                                    fileBrowser.startExtApp(tag.substring(tag.indexOf(" ") + 1, tag.lastIndexOf("_")));
                                }
                                return true;
                            }
                        });

                        ImageView attachedImg = new ImageView(fileBrowser);
                        attachedImg.setLayoutParams(attachedLayParam);
                        attachedImg.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/Attached.png"));


                        attRel[attRel.length - 1].addView(attachedTx);
                        attRel[attRel.length - 1].addView(attachedImg);
                        AttachLin.addView(attRel[attRel.length - 1]);
                    }
                }
            }
            createMail = false;
            mailAttached = false;
        }

        mainLin.addView(TextLin);
        mainLin.addView(AttachLin);

        return mainLin;
    }

    public LinearLayout createHaederMail () {
        icons = new ImageView[0];
        mainLin = new LinearLayout(fileBrowser);
        mainLin.setOrientation(LinearLayout.VERTICAL);
        mainLin.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mainLin.setPadding(10,10,10,10);

        headEMScroll = new HorizontalScrollView(fileBrowser);
        headEMScroll.setHorizontalScrollBarEnabled(false);
        headEMScroll.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth, RelativeLayout.LayoutParams.WRAP_CONTENT));
        headEMScroll.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        headEMScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                if(fileBrowser.showList != null && fileBrowser.showList.isVisible())
                    fileBrowser.fragmentShutdown(fileBrowser.showList,3);
            }
        });
        LinearLayout iconLin = new LinearLayout(fileBrowser);
        iconLin.setLayoutParams(new RelativeLayout.LayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (emailLayout.getHeight() / 9))));
        iconLin.setOrientation(LinearLayout.HORIZONTAL);
        iconLin.setPadding(10,10,10,10);

        for (String s : fileBrowser.file_icon_Loader("Icons/mailIcons")) {
            if (s.contains("closed")) {

                icons = Arrays.copyOf(icons, icons.length + 1);
                icons[icons.length - 1] = new ImageView(fileBrowser);
                icons[icons.length - 1].setLayoutParams(new RelativeLayout.LayoutParams((int) (displayWidth/16 * xfact),(int) (displayWidth/16 * xfact)));
                icons[icons.length - 1].setTag("false " + s);
                icons[icons.length - 1].setEnabled(false);

                if (s.equals(posIcon)) {
                    if(((s.contains("Send") && icons[icons.length - 3].getTag().toString().contains("open")) || (s.contains("Save") &&
                            icons[icons.length - 2].getTag().toString().contains("open")) || (s.contains("Attachment") &&
                            icons[icons.length - 4].getTag().toString().contains("open"))) || !(s.contains("Send") || s.contains("Save") || s.contains("Attachment") ||
                            s.contains("Arrived") || s.contains("Trash"))) {
                        s = s.replace("closed", "open");
                        icons[icons.length - 1].setTag("false " + s);
                    }
                }
                if (s.startsWith("XInfo") && (mailAccountData == null || mailAccountData.length == 0)) {
                    icons[icons.length - 1].setEnabled(true);
                    icons[icons.length - 1].setTag("true " + s);
                } else if ((mailAccountData != null && mailAccountData.length > 0))
                    if(((s.contains("Send") && icons[icons.length - 3].getTag().toString().contains("open")) || (s.contains("Save") &&
                            icons[icons.length - 2].getTag().toString().contains("open")) || (s.contains("Attachment") &&
                            icons[icons.length - 4].getTag().toString().contains("open"))) || !(s.contains("Send") || s.contains("Save") || s.contains("Attachment") ||
                            s.contains("Arrived"))) {
                        icons[icons.length - 1].setEnabled(true);
                    }

                icons[icons.length - 1].setImageBitmap(fileBrowser.bitmapLoader("Icons/mailIcons/" + s));

                if(s.contains("New") && posIcon.contains("Trash") && folderName.startsWith("In")) {
                    fileBrowser.changeIcon(icons[4], "mailIcons", "closed", "open");
                }
                if (!s.contains("Empty"))
                    icons[icons.length - 1].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String tag = v.getTag().toString();

                            if (tag.contains("closed")) {

                                if (tag.contains("Info")) {
                                    createMail = false;
                                    calledBack = "InfoView";
                                    try {
                                        mailAccountData = fileBrowser.read_writeFileOnInternalStorage("read", "AccountData", "MailAccountData.txt", "");
                                    } catch (Exception fe) {}

                                    if (mailAccountData == null || mailAccountData.length == 0)
                                        mailAccountData = fileBrowser.docu_Loader("Language/" + fileBrowser.language + "/MailAccountData.txt");

                                    if (mailAccountData[3].contains("(!") || mailAccountData[4].contains("(!")) {
                                        fileBrowser.messageStarter("Instruction_MailAccount", docu_Loader("Language/" + language + "/Instruction_MailAccount.txt"), 8000);
                                    }


                                    for (int i = 0; i < mailAccountData.length; i++) {
                                        if(mailAccountData[i].contains("   -------------------------------------------   ")) {
                                            mailAccountData[i] = mailAccountData[i].substring(0, mailAccountData[i].length() / 2) + (n1+1) +
                                                    mailAccountData[i].substring(mailAccountData[i].length() / 2);
                                        }
                                        if(i == mailAccountData.length -1)
                                            memoryList[3] = memoryList[3] + "\t" + mailAccountData[i].trim();
                                        else
                                            memoryList[3] = memoryList[3] + "\t" + mailAccountData[i].trim() + "\n";
                                    }

                                } else if (tag.contains("file")) {
                                    createMail = false;
                                    memoryList[3] = "";
                                    float f = 4;
                                    if(yfact <= 0.625)
                                        f = 3;

                                    int[] iconpos = new int[2];
                                    v.getLocationOnScreen(iconpos);

                                    fileBrowser.createList("mailList", 1, "Language/" + fileBrowser.language + "/MailFolderList.txt", 5,
                                                iconpos[0] +v.getWidth() +10, iconpos[1] + v.getHeight()/2, (int)(displayWidth / f), "ru");

                                    fileBrowser.frameLy.get(3).bringToFront();

                                } else if (tag.contains("New")) {
                                    for(int i=1; i<memoryList.length; i++)
                                        memoryList[i] = "";
                                    createMail = true;
                                    folderName = "";
                                    attachedList = new ArrayList<>();
                                } else if (tag.contains("Call")) {
                                    arrayList = new ArrayList<>();
                                    String accountName = "Account ", kind = "mailCallAccountList";
                                    if(fileBrowser.language.equals("Deutsch"))
                                        accountName = "Konto ";
                                    for(int i=0;i<n1; i++) {
                                        arrayList.add(new String[]{accountName + (i + 1)});
                                    }
                                    fileBrowser.changeIcon(v, "mailIcons", "closed","open");

                                    float f = 6;
                                    if(yfact <= 0.625)
                                        f = 5;

                                    int[] iconpos = new int[2];
                                    v.getLocationOnScreen(iconpos);

                                    fileBrowser.createList(kind, 1, "", 5,
                                            iconpos[0] +v.getWidth() +10, iconpos[1] + v.getHeight()/2, (int)(displayWidth / f), "ru");

                                    fileBrowser.frameLy.get(3).bringToFront();

                                    return;
                                } else if (tag.contains("Attachment")) {
                                    attachment = true;
                                    refreshMemoryList();
                                    if(attachedList == null || attachedList.size() == 0)
                                        attachedList = new ArrayList<>();
                                    fileBrowser.changeIcon(v,"mailIcons","closed", "open");
                                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                        arrayList = new ArrayList<>();
                                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                    }
                                    fileBrowser.startMovePanel(5);
                                    return;
                                } else if (tag.contains("Send")) {
                                    refreshMemoryList();
                                    collectionMemoryList = new ArrayList<>();
                                    collectionMemoryList.add(memoryList);

                                    arrayList = new ArrayList<>();
                                    String accountName = "Account ", kind = "mailSentAccountList";
                                    if(fileBrowser.language.equals("Deutsch"))
                                        accountName = "Konto ";
                                    for(int i=0;i<n1; i++) {
                                        arrayList.add(new String[]{accountName + (i + 1)});
                                    }

                                    fileBrowser.changeIcon(v, "mailIcons", "closed","open");

                                    float f = 6;
                                    if(yfact <= 0.625)
                                        f = 5;

                                    int[] iconpos = new int[2];
                                    v.getLocationOnScreen(iconpos);

                                    fileBrowser.createList(kind, 1, "", 5,
                                            iconpos[0] +v.getWidth() +10, iconpos[1] + v.getHeight()/2, (int)(displayWidth / f), "ru");

                                    fileBrowser.frameLy.get(3).bringToFront();

                                    return;
                                } else if (tag.contains("Save")) {
                                    refreshMemoryList();
                                    collectionMemoryList = new ArrayList<>();
                                    collectionMemoryList.add(memoryList);
                                    if(attachedList != null && attachedList.size() > 0 ) {
                                        attachedList = new ArrayList<>();
                                    }
                                    fileBrowser.changeIcon(v,"mailIcons","closed", "open");

                                    float f = 4;
                                    if(yfact <= 0.625)
                                        f = 3;

                                    int[] iconpos = new int[2];
                                    v.getLocationOnScreen(iconpos);

                                    fileBrowser.createList("mailSaveList", 1, "Language/" + fileBrowser.language + "/MailFolderList.txt", 5,
                                            iconpos[0] +v.getWidth() +10, iconpos[1] + v.getHeight()/2, (int)(displayWidth / f), "ru");

                                    fileBrowser.frameLy.get(3).bringToFront();

                                    return;
                                } else if (tag.contains("Trash")) {

                                    fileBrowser.blink = new FileBrowser.blinkIcon(v, "Trash");
                                    fileBrowser.blink.start();

                                    if(!deleteIndividium.equals("")) {
                                        String[] Index = new String[0];
                                        String index1 = "";
                                        if(deleteIndividium.contains(":")) {
                                            Index = fileBrowser.read_writeFileOnInternalStorage("read", deleteIndividium.substring(0,deleteIndividium.indexOf(":")),
                                                    "", "");
                                            for(String s : Index)
                                                if (!s.equals(deleteIndividium.substring(deleteIndividium.indexOf(":") + 1))) {
                                                    index1 = index1 + s + "\n";
                                                }
                                            if(fileBrowser.read_writeFileOnInternalStorage("write", deleteIndividium.substring(0,deleteIndividium.indexOf(":")),
                                                    deleteIndividium.substring(deleteIndividium.indexOf(":") + 1), "delete").length == 0) {
                                                if(folderName.startsWith("Mail")) {
                                                    String folder = "MailEingang";
                                                    if(fileBrowser.language.equals("English"))
                                                        folder = "MailsArrived";
                                                    String[] tmp = new File("/storage/self/primary/tmp/"+folder).list();
                                                    String subject = deleteIndividium;
                                                    if(subject.contains("'"))
                                                        subject = deleteIndividium.substring(deleteIndividium.indexOf("'") +1,
                                                            deleteIndividium.lastIndexOf("'")).replace(" ","");
                                                    subject = subject.substring(subject.indexOf(":") +1).replace("'", "").replace(".", "").replace(" ", "")
                                                            .replace("’", "").replace(",","").replace("(","")
                                                            .replace(")","").replace("?","").replace("/","").trim();

                                                    for(String s: tmp)
                                                        if(s.contains("Attached")) {
                                                            if (s.contains(subject)) {
                                                                new File("/storage/self/primary/tmp/" + folder + "/" + s).delete();
                                                            }
                                                        }
                                                    String deleteSubj = deleteIndividium.substring(deleteIndividium.indexOf(" >>")+3);
                                                    deleteSubj = deleteSubj.replace("°°°","").replace(" '","");
                                                    deleteIndividium = deleteIndividium.replace("°","/");
                                                    new sendThread("Delete " + deleteSubj).start();
                                                }

                                            }
                                        } else if(deleteIndividium.contains("/")) {
                                            Index = fileBrowser.read_writeFileOnInternalStorage("read", "ToAddresses","emailAddresses.txt","");

                                            for(String s : Index)
                                                if (!s.equals(deleteIndividium.substring(deleteIndividium.indexOf("/") + 1))) {
                                                    index1 = index1 + s + "\n";
                                                }
                                            fileBrowser.read_writeFileOnInternalStorage("write", "ToAddresses","emailAddresses.txt", index1);
                                            if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                                arrayList = new ArrayList<>();
                                                fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                            }
                                            fileBrowser.changeIcon(toChoose,"browserIcons","open", "closed");
                                        } else if(deleteIndividium.contains(" ")) {
                                            attachedList.remove(Integer.parseInt(deleteIndividium.substring(0,deleteIndividium.indexOf(" "))));
                                        }
                                        fileBrowser.messageStarter("mailDelete", docu_Loader("Language/" + language + "/Successful_Action.txt"), 5000);
                                    }
                                    if(!deleteIndividium.contains("attachedList")) {
                                        deleteIndividium = "";
                                        return;
                                    }
                                    createMail = true;
                                    deleteIndividium = "";
                                    refreshMemoryList();
                                }

                                posIcon = tag.substring(tag.indexOf(" ") + 1);
                                if (fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                    arrayList = new ArrayList<>();
                                    fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                }

                            } else if (tag.contains("open")) {
                                if (tag.contains("Info_open")) {
                                    calledBack = "";
                                    String[] nextAccount = fileBrowser.docu_Loader("Language/" + fileBrowser.language + "/MailAccountData.txt");
                                    nextAccount = Arrays.copyOfRange(nextAccount,2, nextAccount.length);
                                    if (!mailTx.getText().toString().contains("(!")) {
                                        mailTx.getText().toString().trim();
                                        for(int i=0;i<3;i++)
                                            if(mailTx.getText().toString().endsWith("\n"))
                                               mailTx.setText(mailTx.getText().toString().substring(0, mailTx.getText().toString().lastIndexOf("\n")));
                                        for(int i=0;i<nextAccount.length;i++)
                                            mailTx.setText(mailTx.getText().toString() +"\n" + nextAccount[i]);
                                        mailAccountData = mailTx.getText().toString().split("\n");
                                        fileBrowser.read_writeFileOnInternalStorage("write", "AccountData","MailAccountData.txt", mailTx.getText().toString());
                                        n1++;
                                    } else {
                                        String[] accCheck = fileBrowser.read_writeFileOnInternalStorage("read", "AccountData","MailAccountData.txt","");
                                        String[] mailTxCheck = mailTx.getText().toString().split("\n");
                                        int i = 0;
                                        boolean b = false;
                                        for(String s: accCheck) {
                                            if (!s.equals(mailTxCheck[i])) {
                                                accCheck[i] = mailTxCheck[i];
                                                b = true;
                                            }
                                            i++;
                                        }

                                        if(b) {
                                            String acc = "";
                                            for(String s: accCheck)
                                                acc = acc + s +"\n";

                                            if(!acc.equals("")) {
                                                mailTx.setText(acc.substring(0, acc.lastIndexOf("\n")));
                                                mailAccountData = mailTx.getText().toString().split("\n");
                                                fileBrowser.read_writeFileOnInternalStorage("write", "AccountData","MailAccountData.txt", mailTx.getText().toString());
                                            }
                                        }

                                    }
                                    memoryList[3] = "";
                                } else if (tag.contains("file")) {
                                    fileBrowser.selectedTx02 = -1;
                                    memoryList[3]="";
                                    mailTx.setText("");
                                    if (fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                        arrayList = new ArrayList<>();
                                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                    }
                                } else if (tag.contains("Send")) {
                                    fileBrowser.threadStop = true;
                                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                        fileBrowser.changeIcon(v, "mailIcons", "open","closed");
                                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                    }
                                    if(fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible()) {
                                        fileBrowser.changeIcon(v, "mailIcons", "open","closed");
                                        fileBrowser.fragmentShutdown(fileBrowser.showMessage, 0);
                                    }
                                    return;
                                } else if (tag.contains("Save")) {
                                    fileBrowser.changeIcon(v,"mailIcons","open", "closed");
                                    if (fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                        arrayList = new ArrayList<>();
                                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                    }
                                    return;
                                } else if (tag.contains("New")) {
                                    for(int i=1; i<memoryList.length; i++)
                                        memoryList[i] = "";
                                    attachedList = new ArrayList<>();
                                    createMail = false;
                                } else if (tag.contains("Attachment")) {
                                    attachment = false;
                                    return;
                                } else if (tag.contains("Trash")) {
                                    fileBrowser.changeIcon(v,"mailIcons","open", "closed");
                                    return;
                                } else if (tag.contains("Call")) {
                                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                        fileBrowser.changeIcon(v, "mailIcons", "open","closed");
                                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                    }
                                    return;
                                }

                                posIcon = "";
                            }

                            createNewDisplay();
                        }
                    });
                iconLin.addView(icons[icons.length - 1]);
            }
        }
        headEMScroll.addView(iconLin);
        mainLin.addView(headEMScroll);
        return mainLin;
    }

    public void createMailBack() {
        ImageView mailBackImg = new ImageView(fileBrowser);
        mailBackImg.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth/10, displayWidth/10));
        mailBackImg.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/email_back.png"));
        mailBackImg.setX(2*displayWidth/3);
        mailBackImg.setY(displayHeight/3);
        mailBackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLin.removeView(header);
                mainLin.removeView(TextLin);
                mainLin.removeView(AttachLin);
                attachedList = new ArrayList<>();
                int f=4;
                if(yfact <= 0.625)
                    f=3;

                fileBrowser.createList("mailList", 1, "Language/" + fileBrowser.language + "/MailFolderList.txt", 5,
                        (int) (fileBrowser.createSendEmail.icons[0].getX() + fileBrowser.createSendEmail.icons[0].getWidth()),
                        ((int) fileBrowser.createSendEmail.icons[0].getY() + fileBrowser.createSendEmail.icons[0].getHeight() + 35), fileBrowser.displayWidth / f,
                        "ru");
                fileBrowser.frameLy.get(3).bringToFront();

                fileBrowser.changeIcon(headMenueIcon[6],"headMenueIcons", "open","closed");
                fileBrowser.changeIcon(headMenueIcon02[5],"sideRightMenueIcons", "open","closed");


                showFolderIndex(folderName);
                mainRel.removeView(view);
            }
        });

        mainRel.addView(mailBackImg);
    }

    public void handleSendThread(String kind_of) {
        new emailDisplayFragment.sendThread(kind_of).start();
    }

    public class HandleMailWithAttachmen extends AsyncTask<Void, Void, Void> {
        String[] memList;

        @Override
        protected Void doInBackground(Void... params) {
            fileBrowser.blink = new FileBrowser.blinkIcon(fileBrowser.createSendEmail.icons[fileBrowser.createSendEmail.icons.length - 3], "Send");
            fileBrowser.blink.start();
            nn = nn*8;
            if(mailAccountData.length >= nn) {

                String to = "", host_out = "", password = "", used_from = "", port = "";
                if (!mailAccountData[3].contains("(!") && !mailAccountData[4].contains("(!")) {
                    to = memoryList[1];
                    used_from = memoryList[0];

                    host_out = mailAccountData[3 + nn].split(":")[1].trim().replace("(", "").replace(")", "");
                    password = mailAccountData[8 + nn].split(":")[1].trim().replace("(", "").replace(")", "");
                    port = "587";

                    if (host_out.contains("protonmail")) {
                        fileBrowser.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fileBrowser.threadStop = true;
                                fileBrowser.startExtApp("https://mail.protonmail.com/u/0/inbox");
                            }
                        });
                        return null;
                    }
                }

                final String passwd = password, user = used_from;

                Properties properties = new Properties();
                properties.put("mail.smtp.host", host_out);
                properties.put("mail.smtp.port", port);
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");


                Session session = Session.getInstance(properties,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(user, passwd);
                            }
                        });

                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(user));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                    message.setSubject(memoryList[2]);

                    BodyPart messageBodyPart1 = new MimeBodyPart();
                    messageBodyPart1.setText(memoryList[3]);

                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(messageBodyPart1);

                    MimeBodyPart[] mimeBodyParts = new MimeBodyPart[0];

                    for (int i = 0; i < attachedList.size(); i++) {
                        mimeBodyParts = Arrays.copyOf(mimeBodyParts, mimeBodyParts.length + 1);
                        mimeBodyParts[mimeBodyParts.length - 1] = new MimeBodyPart();

                        String filename = attachedList.get(i)[1];
                        DataSource source = new FileDataSource(attachedList.get(i)[0] + "/" + attachedList.get(i)[1]);
                        mimeBodyParts[mimeBodyParts.length - 1].setDataHandler(new DataHandler(source));
                        mimeBodyParts[mimeBodyParts.length - 1].setFileName(filename);

                        multipart.addBodyPart(mimeBodyParts[mimeBodyParts.length - 1]);
                    }

                    message.setContent(multipart);

                    Transport.send(message);

                    fileBrowser.messageStarter("mailSendRequest", docu_Loader("Language/" + language + "/mailSent.txt"), 5000);


                    String folder = "Sent Mails";
                    if (language.equals("Deutsch"))
                        folder = "Gesendete Mails";

                    saveEmail(folder);
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }

        public void handleMessages(String subjectTo) {
            String kindOfAction = "";
            collectionMemoryList = new ArrayList<>();
            memList = new String[0];
            nn = nn*8;
            String host_in = "", used_from = "", port = "", password = "";
            if(mailAccountData.length >= nn){

                used_from = mailAccountData[6 + nn].split(":")[1].trim().replace("(", "").replace(")", "");
                host_in = mailAccountData[4 + nn].split(":")[1].trim().replace("(", "").replace(")", "");
                password = mailAccountData[8 + nn].split(":")[1].trim().replace("(", "").replace(")", "");
                port = mailAccountData[9 + nn].split(":")[1].trim().replace("(", "").replace(")", "");
                Folder inbox;
                if (host_in.contains("protonmail")) {
                    fileBrowser.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileBrowser.threadStop = true;
                            fileBrowser.startExtApp("https://mail.protonmail.com/u/0/inbox");
                        }
                    });

                    return;
                }
                Properties properties = new Properties();

                // server setting
                properties.put("mail.imap.host", host_in);
                properties.put("mail.imap.port", port);

                // SSL setting
                properties.setProperty("mail.imap.socketFactory.class",
                        "javax.net.ssl.SSLSocketFactory");
                properties.setProperty("mail.imap.socketFactory.fallback", "false");
                properties.setProperty("mail.imap.socketFactory.port", port);

                Session session = Session.getDefaultInstance(properties);

                try {
                    // connects to the message store
                    Store store = session.getStore("imap");
                    store.connect(used_from, password);

                    // opens the inbox folder
                    Folder folderInbox = store.getFolder("INBOX");
                    folderInbox.open(Folder.READ_WRITE);

                    // fetches new messages from server
                    Message[] arrayMessages = folderInbox.getMessages();

                    for (int i = 0; i < arrayMessages.length; i++) {
                        Message message = arrayMessages[i];

                        if (subjectTo.equals("")) {
                            printEnvelope(message);
                            collectionMemoryList.add(memList);
                            memList = new String[0];
                        } else {
                            String subject = message.getSubject();
                            subjectTo = subjectTo.replace("Delete ", "");
                            if (subject.contains(subjectTo)) {
                                kindOfAction = "delete";
                                message.setFlag(Flags.Flag.DELETED, true);
                            }
                        }

                    }
                    if (collectionMemoryList.size() > 0) {
                        String folder = "Mails Arrived";
                        if (fileBrowser.language.equals("Deutsch"))
                            folder = "Mail Eingang";

                        saveEmail(folder);
                        fileBrowser.changeIcon(icons[2], "mailIcons", "closed", "open");

                    } else if (!kindOfAction.equals("delete")) {
                        boolean expunge = true;
                        folderInbox.close(expunge);
                        fileBrowser.messageStarter("mailNoDelivered", docu_Loader("Language/" + language + "/mailNoDelivered.txt"), 5000);
                    }

                    // disconnect
                    store.close();
                } catch (NoSuchProviderException ex) {
                    System.out.println("No provider.");
                    ex.printStackTrace();
                } catch (MessagingException ex) {
                    System.out.println("Could not connect to the message store.");
                    String[] message = docu_Loader("Language/" + language + "/mailNoAccountPermission.txt");
                    message = Arrays.copyOf(message, message.length + 2);
                    message[message.length - 2] = host_in.substring(host_in.indexOf(".") + 1).toUpperCase();
                    message[message.length - 1] = ex.toString();
                    fileBrowser.messageStarter("mailNoAccountPermission", message, 8000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void printEnvelope(Message message) throws Exception {
            textIsHtml = false;
            Address[] a;
            Date receivedDate = message.getReceivedDate();
            String time = receivedDate.toString().substring(receivedDate.toString().indexOf(":")-2,
                    receivedDate.toString().indexOf("GMT"));
            // FROM
            if ((a = message.getFrom()) != null) {

                for (int j = 0; j < a.length; j++) {
                    memList = Arrays.copyOf(memList, memList.length + 1);
                    memList[memList.length - 1] = receivedDate.toString().substring(0, receivedDate.toString().indexOf(":")-2)  + " -> " + a[j].toString();
                }
            } else {
                memList = Arrays.copyOf(memList, memList.length + 1);
                memList[memList.length - 1] = "";
            }
            // TO
            if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
                for (int j = 0; j < a.length; j++) {
                    memList = Arrays.copyOf(memList, memList.length + 1);
                    memList[memList.length - 1] = a[j].toString();
                }
            } else {
                memList = Arrays.copyOf(memList, memList.length + 1);
                memList[memList.length - 1] = "";
            }
            Object msgContent = message.getContent();
            String contentSubject = message.getSubject(),
                   content = "",
                    contentType = message.getContentType();

            //Subject
            if(contentSubject != null && contentSubject.length() > 0) {
                if (contentSubject.length() > 60)
                    contentSubject = contentSubject.substring(0, 60)+"°°°";
                memList = Arrays.copyOf(memList, memList.length + 1);
                memList[memList.length - 1] = "' " + time +">>"+ contentSubject + " '";
            } else {
                memList = Arrays.copyOf(memList, memList.length + 1);
                memList[memList.length - 1] = time + "";
            }
            /* Check if content is pure text/html or in parts */
            if (msgContent instanceof Multipart) {
                Multipart multipart = (Multipart) msgContent;

                for (int j = 0; j < multipart.getCount(); j++) {

                    BodyPart bodyPart = multipart.getBodyPart(j);
                    String disposition = bodyPart.getDisposition();

                    if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {
                            isAttached(message, bodyPart, time, contentSubject);
                    }
                    else if(contentType.contains("multipart")) {
                        content = getText(message);
                    }

                }

            }
            else {
                content = message.getContent().toString();
            }
                memList = Arrays.copyOf(memList, memList.length + 1);
                memList[memList.length - 1] = content;
        }
        private ByteArrayOutputStream inToStream(InputStream in) throws IOException {

            BufferedInputStream bis = new BufferedInputStream(in);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result = bis.read();
            while(result != -1) {
                byte b = (byte)result;
                buf.write(b);
                result = bis.read();
            }

            return buf;
        }

        private String getText(Part p) throws
                MessagingException, IOException {
            textIsHtml = false;
            if (p.isMimeType("text/*")) {
                String s = (String)p.getContent();
                return s;
            }

            if (p.isMimeType("multipart/alternative")) {
                Multipart mp = (Multipart)p.getContent();
                String text = null;
                for (int i = 0; i < mp.getCount(); i++) {
                    Part bp = mp.getBodyPart(i);
                    if (bp.isMimeType("text/plain")) {
                        if (text == null)
                            text = getText(bp);
                        continue;
                    } else if (bp.isMimeType("text/html")) {
                        textIsHtml = true;
                        String s = getText(bp);
                        if (s != null)
                            return s;
                    } else {
                        return getText(bp);
                    }
                }
                return text;
            } else if (p.isMimeType("multipart/*")) {
                Multipart mp = (Multipart)p.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    String s = getText(mp.getBodyPart(i));
                    if (s != null)
                        return s;
                }
            }

            return null;
        }
    }

    private void isAttached  (Message message, BodyPart bodyPart, String arrTime, String subj) {
        String trans = subj.replace("'", "").replace(".", "").replace(" ", "")
                .replace("’", "").replace(",","").replace("(","")
                .replace(")","").replace("?","").replace("/","")
                .replace(":","").trim();
        DataHandler handler = null;
        try {
            handler = bodyPart.getDataHandler();

        } catch(MessagingException me) {
            return;
        }

        String folder = "MailEingang",
                kindOfFile = handler.getName(),
                savefileName = "." +arrTime.trim()+">>"+trans+
                        "_AttachedFile" + kindOfFile.substring(kindOfFile.lastIndexOf("."));
        if(fileBrowser.language == "English")
            folder = "MailsArrived";

        try {
            File dir = new File("/storage/self/primary/tmp/" +
                    folder);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File ofile = new File(dir, savefileName);

            InputStream is = handler.getInputStream();
            OutputStream ostream = new FileOutputStream(ofile);
            byte[] data = new byte[4096];
            int r = 0;
            while ((r = is.read(data, 0, data.length)) != -1) {
                ostream.write(data, 0, r);
            }
            ostream.flush();
            ostream.close();

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        Log.e("Deliverd: -> ", savefileName);

    }

    class sendThread extends Thread {
        String kind="";
        public sendThread(String kindOf) {kind = kindOf;}

        public void run () {
            try {
                for(int i=0;i<3; i++) {
                    if (fileBrowser.haveNetwork()) {
                        if(kind.equals("Sent"))
                            fileBrowser.messageStarter("mailSendRequest", docu_Loader("Language/" + language + "/MailSendRequest.txt"), 0);
                        else if(kind.equals("Call"))
                            new HandleMailWithAttachmen().handleMessages("");
                        else if(kind.startsWith("Delete")) {
                            new HandleMailWithAttachmen().handleMessages(kind);
                        }
                        break;
                    } else {
                        if(i >= 2) {
                            fileBrowser.messageStarter("mailNoInternet", docu_Loader("Language/" + language + "/NoInternet_avaliable.txt"), 6000);
                            fileBrowser.threadStop = true;
                            break;
                        }
                        Thread.sleep(3000);
                    }
                }
            } catch (InterruptedException ie) {}

        }
    }
}
