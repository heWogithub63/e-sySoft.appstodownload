package easysoft.freebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static easysoft.freebrowser.FileBrowser.*;
import static easysoft.freebrowser.TextEditorFragment.logoPath;
import static easysoft.freebrowser.emailDisplayFragment.nn;


public class showListFragment extends Fragment {
    View view;
    FrameLayout showListLayout;
    ScrollView gridScr;
    int columnCount = 0, gridCol = 0, height = displayHeight/8;
    static int selectedTx_01 = 0, scrPosY = 0;
    static String caller = "";
    static TextView[] listTx;
    static ImageView[] folderImg;

    int txColor, backgrColor;
    public showListFragment() {

    }

    public static showListFragment newInstance() {
        showListFragment fragment = new showListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showListLayout = fileBrowser.frameLy.get(3);
            columnCount = getArguments().getInt("COLUMN_COUNT");
            caller = getArguments().getString("CALLER");
            gridCol = columnCount;
            if(caller.equals("mailList")) {
                folderImg = new ImageView[0];
                gridCol = 2;
            }
            if(showListLayout.getHeight() != 0)
               height = showListLayout.getHeight();
            if(caller.equals("mediaList"))
                height = 2*height;


        }

        if(caller.equals("startList") || caller.startsWith("mediaList") || caller.startsWith("documentList") ||
                caller.startsWith("Trash") || caller.contains("mediaCollectionList") || caller.contains("searchMachineList")) {
            backgrColor = getResources().getColor(R.color.white);
            txColor = getResources().getColor(R.color.black);
        } else  {
            backgrColor = getResources().getColor(R.color.black_overlay);
            txColor = getResources().getColor(R.color.white);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_list, container, false);
        RelativeLayout mainRel = (RelativeLayout) view.findViewById(R.id.mainRel);

        gridScr = new ScrollView(fileBrowser);
        gridScr.setLayoutParams(new RelativeLayout.LayoutParams(showListLayout.getWidth(), height));
        gridScr.post(new Runnable() {
            public void run() {
                gridScr.smoothScrollBy(0, scrPosY);
            }
        });
        gridScr.addView(createList());
        mainRel.addView(gridScr);

        showListLayout.bringToFront();
        return view;
    }

    private GridLayout createList() {

        GridLayout maingrid = new GridLayout(fileBrowser);
        maingrid.setRowCount(arrayList.size());
        maingrid.setColumnCount(gridCol);
        maingrid.setLayoutParams(new RelativeLayout.LayoutParams(showListLayout.getWidth(), RelativeLayout.LayoutParams.WRAP_CONTENT));
        maingrid.setBackgroundColor(backgrColor);

        listTx = new TextView[0];
        int hlength = arrayList.size();
        if(arrayList.size() >= 5) {
            hlength = 4;
        }
              for(int i=0; i<arrayList.size(); i++) {
                  for(int i1=0;i1<columnCount;i1++) {
                      listTx = Arrays.copyOf(listTx, listTx.length + 1);
                      listTx[listTx.length - 1] = new TextView(fileBrowser);
                      listTx[listTx.length - 1].setTextSize((float) (textSize));
                      listTx[listTx.length - 1].setTextColor(txColor);
                      listTx[listTx.length - 1].setLayoutParams(new RelativeLayout.LayoutParams(showListLayout.getWidth() /columnCount,
                              height / hlength));
                      listTx[listTx.length - 1].setTag(listTx.length - 1 +" "+ arrayList.get(i)[i1] +"_closed");
                      listTx[listTx.length - 1].setY(10);
                      if((listTx.length -1 == selectedTx && caller.equals("startListUrl")) ||
                              listTx.length -1 == selectedTx_01 && caller.equals("PdfSideList") ||
                              listTx.length -1 == selectedTx02 && caller.equals("mailList"))
                          listTx[listTx.length - 1].setTextColor(getResources().getColor(R.color.green));

                      if(((!canWrite || !insertaction)  && (arrayList.get(i)[i1].contains("Einfügen") || arrayList.get(i)[i1].contains("Paste")))  ||
                              (isPdf && (arrayList.get(i)[i1].equals("Text Datei") || arrayList.get(i)[i1].contains("import")))) {
                          listTx[listTx.length - 1].setTextColor(getResources().getColor(R.color.grey));
                          listTx[listTx.length - 1].setEnabled(false);
                      }
                      listTx[listTx.length - 1].setText("\t" +arrayList.get(i)[i1]);

                      if(caller.equals("startList")) {
                          if(!canWrite && (arrayList.get(i)[i1].contains("Kopieren") || arrayList.get(i)[i1].contains("Copy") ||
                                  arrayList.get(i)[i1].contains("Verschieben") || arrayList.get(i)[i1].contains("Move"))) {
                                      listTx[listTx.length - 1].setText("\t" +arrayList.get(i)[i1] +" -> Intern");
                          }
                          else if(!canWrite && (arrayList.get(i)[i1].contains("Umbenennen") || arrayList.get(i)[i1].contains("Rename") ||
                                  arrayList.get(i)[i1].contains("Löschen") || arrayList.get(i)[i1].contains("Delete") ||
                                  arrayList.get(i)[i1].contains("-->") ||
                                  arrayList.get(i)[i1].contains("Neuer Ordner") || arrayList.get(i)[i1].contains("New Folder"))) {
                              listTx[listTx.length - 1].setTextColor(getResources().getColor(R.color.grey));
                              listTx[listTx.length - 1].setEnabled(false);
                          }
                      } else if(caller.equals("searchMachineList")) {
                          listTx[listTx.length - 1].setTag(listTx.length - 1 +" "+ listTx[listTx.length - 1].getText().toString().substring(listTx[listTx.length - 1].getText().toString().indexOf(" ") +1));
                          listTx[listTx.length - 1].setText(listTx[listTx.length - 1].getText().toString().substring(0,listTx[listTx.length - 1].getText().toString().indexOf(" ")));
                      }
                      if(fileBrowser.createTxEditor != null && fileBrowser.createTxEditor.isVisible() && (fileBrowser.createTxEditor.isBackground && arrayList.get(i)[i1].contains("Text ") ||
                              (fileBrowser.createTxEditor.isLogo && arrayList.get(i)[i1].contains("Text ")))) {
                          listTx[listTx.length - 1].setTextColor(getResources().getColor(R.color.grey));
                          listTx[listTx.length - 1].setEnabled(false);
                      }

                      if(yfact > 0.625) {
                          if (caller.equals("webSideMemoryList") && arrayList.get(i)[i1].length() > 40)
                              listTx[listTx.length - 1].setText("\t" + arrayList.get(i)[i1].substring(0, 40));
                      } else {
                          if (caller.equals("webSideMemoryList") && arrayList.get(i)[i1].length() > 26)
                              listTx[listTx.length - 1].setText("\t" + arrayList.get(i)[i1].substring(0, 26));
                      }


                      if (i1==0) {
                          if(caller.equals("webSideMemoryList"))
                              listTx[listTx.length - 1].setOnLongClickListener(new View.OnLongClickListener() {
                                  @Override
                                  public boolean onLongClick(View view) {

                                      if(fileBrowser.webBrowserDisplay != null && fileBrowser.webBrowserDisplay.isVisible()) {

                                          fileBrowser.webBrowserDisplay.ishandled = true;
                                          String tag1 = view.getTag().toString().substring(view.getTag().toString().indexOf(" ") +1,
                                                  view.getTag().toString().lastIndexOf("_"));
                                          fileBrowser.webBrowserDisplay.webView.loadUrl(tag1);
                                          fileBrowser.changeIcon(fileBrowser.webBrowserDisplay.steerImgs[2], "browserIcons", "open", "closed");
                                          showListLayout.removeAllViews();
                                          fileBrowser.fragmentShutdown(fileBrowser.showList,3);
                                          fileBrowser.changeIcon(fileBrowser.webBrowserDisplay.steerImgs[3], "browserIcons", "open", "closed");
                                      }

                                      return true;
                                  }
                              });
                          listTx[listTx.length - 1].setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {
                                  String tag = v.getTag().toString();
                                  String kind_of = ((TextView) v).getText().toString().trim();

                                   for (int i = 0; i < listTx.length; i++) {
                                      listTx[i].setTextColor(txColor);
                                      listTx[i].setTag(listTx[i].getTag().toString().replace("open","closed"));
                                      listTx[i].setEnabled(true);
                                      if((!isPdf && listTx[i].getText().toString().contains("PDF ")) || (isPdf && !listTx[i].getText().toString().contains("PDF "))) {
                                          listTx[i].setEnabled(false);
                                          listTx[i].setTextColor(getResources().getColor(R.color.grey));
                                      }
                                      if(!canWrite && ((listTx[i].getText().toString().contains("Einfügen") || listTx[i].getText().toString().contains("Paste")) ||
                                              (listTx[i].getText().toString().contains("Umbenennen") || listTx[i].getText().toString().contains("Rename")) ||
                                              (listTx[i].getText().toString().contains("Löschen") || listTx[i].getText().toString().contains("Delete")) ||
                                              (listTx[i].getText().toString().contains("Neuer") || listTx[i].getText().toString().contains("New")) ||
                                              listTx[i].getText().toString().contains("-->"))) {
                                          listTx[i].setTextColor(getResources().getColor(R.color.grey));
                                          listTx[i].setEnabled(false);
                                      }
                                      if (canWrite && (kind_of.contains("Umbenennen") || kind_of.contains("Rename") ||
                                              (kind_of.contains("Löschen") || kind_of.contains("Remove"))) &&
                                              (!insertaction && (listTx[i].getText().toString().contains("Einfügen") || listTx[i].getText().toString().contains("Paste"))))  {
                                          listTx[i].setTextColor(getResources().getColor(R.color.grey));
                                          listTx[i].setEnabled(false);
                                      }
                                      if(listTx[i].getTag().equals("")) {
                                          listTx[i-1].setEnabled(false);
                                          listTx[i-1].setTextColor(getResources().getColor(R.color.grey));
                                      }

                                  }

                                  v.setTag(v.getTag().toString().replace("closed", "open"));
                                  ((TextView) v).setTextColor(getResources().getColor(R.color.green));


                                  if(caller.equals("startListUrl")) {
                                      selectedTx = Integer.parseInt(tag.substring(0, tag.indexOf(" ")));
                                      fileBrowser.urldevice = listTx[Integer.parseInt( tag.substring(0, tag.indexOf(" "))) +1].getTag().toString();
                                      fileBrowser.devicePath = fileBrowser.urldevice;
                                      paramList = fileBrowser.createArrayList(fileBrowser.urldevice);
                                      devicePath_trans = urldevice;
                                      if(new File(urldevice).canWrite())
                                          canWrite = true;
                                      else canWrite = false;
                                      fileBrowser.createFolder(urldevice);
                                      if(fileBrowser.filebrowser_01 != null && fileBrowser.filebrowser_01.isVisible())
                                          fileBrowser.fragmentShutdown(fileBrowser.filebrowser_01, 1);
                                  } else if(caller.equals("startList")) {
                                      if(calledBy.equals("Edit-Search")) {
                                          insertaction = false;
                                          if ((kind_of.contains("Kopieren") || kind_of.contains("Copy")) ||
                                                  (kind_of.contains("Verschieben") || kind_of.contains("Move"))) {
                                             insertaction = true;
                                              if (kind_of.contains("Kopieren") || kind_of.contains("Copy")) {
                                                  kind_of = "Copy";
                                                  //commandString = "copy  " + devicePath_trans;
                                                  commandString = "cp -r  " + devicePath_trans;
                                              } else {
                                                  kind_of = "askForMove";
                                                  //commandString = "move  " + devicePath_trans;
                                                  commandString = "mvcp -r  " + devicePath_trans;
                                              }
                                              fileBrowser.messageStarter("Instruction", docu_Loader("Language/" + language + "/Instruction_" +
                                                              kind_of + ".txt"),  5000);

                                          } else if ((kind_of.contains("Umbenennen") || kind_of.contains("Rename"))) {
                                              fileBrowser.blink = new FileBrowser.blinkIcon(v, "FileAction");
                                              fileBrowser.blink.start();
                                              kind_of = "askForRename";
                                              //commandString = "rename  " + devicePath_trans;
                                              commandString = "mvcp -r  " + devicePath_trans;
                                              fileBrowser.messageStarter(kind_of, docu_Loader("Language/" + language + "/Instruction_Rename.txt"),  0);
                                          } else if ((kind_of.contains("Löschen") || kind_of.contains("Delete"))) {
                                              fileBrowser.blink = new FileBrowser.blinkIcon(v, "FileAction");
                                              fileBrowser.blink.start();
                                              kind_of = "_askForDelete";
                                              //commandString = "delete  " + devicePath_trans;
                                              commandString = "rm -rf  " + devicePath_trans;
                                              fileBrowser.messageStarter(kind_of, docu_Loader("Language/" + language + "/Instruction_Delete.txt"), 0);
                                          } else if (kind_of.contains("-->")) {
                                              fileBrowser.blink = new FileBrowser.blinkIcon(v, "FileAction");
                                              fileBrowser.blink.start();
                                              /*String toTrash = fileBrowser.context.getFilesDir() +"/.TrashIndex";
                                              if(!devicePath_trans.substring(devicePath_trans.lastIndexOf("/") +1).contains("."))*/
                                              String toTrash = fileBrowser.context.getFilesDir() +"/.TrashIndex" + devicePath_trans.substring(devicePath_trans.lastIndexOf("/"));
                                              commandString = "move  " +devicePath_trans;
                                              //new FileBrowser.dataHandler("move  ", devicePath_trans, toTrash,null).start();
                                              fileBrowser.runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      fileBrowser.startTerminalCommands("mvcp -r ", devicePath_trans, toTrash);
                                                  }
                                              });
                                              return;
                                          } else if ((kind_of.contains("Neuer Ordner") || kind_of.contains("New Folder"))) {
                                              fileBrowser.blink = new FileBrowser.blinkIcon(v, "FileAction");
                                              fileBrowser.blink.start();
                                              kind_of = "askForCreate";
                                              //commandString = "create  " + devicePath_trans;
                                              commandString = "mkdir  " + devicePath;
                                              fileBrowser.messageStarter(kind_of, docu_Loader("Language/" + language + "/Instruction_CreateFolder.txt"),  0);
                                          } else if ((kind_of.contains("Suchen") || kind_of.contains("Search"))) {
                                              fileBrowser.blink = new FileBrowser.blinkIcon(v, "FileAction");
                                              fileBrowser.blink.start();
                                              kind_of = "askForSearch";
                                              commandString = "ls -aR  " +devicePath_trans;
                                              fileBrowser.messageStarter(kind_of, docu_Loader("Language/" + language + "/Instruction_Search.txt"),  0);
                                          } else if ((kind_of.contains("Einfügen") || kind_of.contains("Paste")) && !commandString.equals("")) {
                                              fileBrowser.blink = new FileBrowser.blinkIcon(v, "FileAction");
                                              fileBrowser.blink.start();

                                              //
                                              String todo = commandString.substring(0, commandString.indexOf("  ")),
                                                      from = commandString.substring(commandString.indexOf("  ") + 2),
                                                      kind = from.substring(from.lastIndexOf("/") +1),
                                                      to = devicePath;
                                              if(kind.contains(" ")) {
                                                  kind = kind.replace(" ","\\ ");
                                              }
                                              to = to +"/"+ kind;
                                              final String to1 = to;

                                              commandString = "";
                                              //new FileBrowser.dataHandler(todo,from,to, null).start();
                                              fileBrowser.runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      fileBrowser.startTerminalCommands(todo, from, to1);
                                                  }
                                              });
                                          }
                                      }
                                  } else if(caller.equals("mediaCollectionList")) {
                                      String kind_offSelection = ((TextView)v).getText().toString();
                                      String Praefix = "";

                                      if(transList == null || transList.size() == 0) {
                                          Praefix = devicePath +"/";
                                          transList = new ArrayList<>();
                                          if(paramList != null && paramList.size() > 0)
                                              for(String s : paramList.get(paramList.size() - 1))
                                                  transList.add(s);
                                      }

                                      runningMediaList = new ArrayList<>();
                                      switch (kind_offSelection.trim()) {
                                          case ("Bilder"): case("Pictures"): {
                                              for(String sL : transList)
                                                  if(sL.endsWith(".gif") || sL.endsWith(".png") || sL.endsWith(".jpg") || sL.endsWith(".jpeg") ||
                                                          sL.endsWith(".JPG"))
                                                      runningMediaList.add(Praefix + sL);
                                              break;
                                          }
                                          case ("Musik-Dateien"): case("Music-Files"): {
                                              for(String sL : transList)
                                                  if(sL.endsWith(".flac") || sL.endsWith(".ogg") || sL.endsWith(".wav") || sL.endsWith(".mp3"))
                                                      runningMediaList.add(Praefix + sL);
                                              break;
                                          }
                                          case ("Video"):  {
                                              for(String sL : transList)
                                                  if(sL.endsWith(".mkv") || sL.endsWith(".mp4") || sL.endsWith(".3gp") || sL.endsWith(".mpg") || sL.endsWith(".mpeg") ||
                                                          sL.endsWith(".webm") || sL.endsWith(".Webm") || sL.endsWith(".avi") || sL.endsWith(".flc"))
                                                      runningMediaList.add(Praefix + sL);
                                              break;
                                          }
                                      }
                                      transList = new ArrayList<>();
                                      fileBrowser.fragmentShutdown(fileBrowser.showList, 3);

                                      if(runningMediaList.size() == 0)
                                          fileBrowser.messageStarter("NoList", docu_Loader("Language/" + language + "/NoFound_MediaList.txt"), 5000);
                                      else {
                                          fileBrowser.intendStarted = true;
                                          fileBrowser.messageStarter("MediaList: " + kind_offSelection, docu_Loader("Language/" + language + "/Created_MediaList.txt"),  3000);
                                      }
                                  } else if(caller.equals("TrashList")) {
                                      if(tag.substring(tag.indexOf(" ")+1).contains(" ")) {
                                          kind_of = "_askTrashDelete";
                                          //commandString = fileBrowser.context.getFilesDir() + "/.TrashIndex";
                                          commandString = "rm -rfx  " + fileBrowser.context.getFilesDir() + "/.TrashIndex";
                                          fileBrowser.messageStarter(kind_of, docu_Loader("Language/" + language + "/Instruction_Delete.txt"), 0);
                                      } else {
                                          calledBy = "TreshIndex";

                                          String[] saveTx =  fileBrowser.read_writeFileOnInternalStorage("read","pathCollection", "PathList.txt", "");
                                          arrayList = new ArrayList<>(0);
                                          for(String s : saveTx) {
                                              if(s.contains("/"))
                                                  arrayList.add(new String[]{s.substring(s.lastIndexOf("/") +1), s.substring(0, s.lastIndexOf("/"))});
                                          }

                                          float f = 3;
                                          if(yfact <= 0.625)
                                              f = 2;
                                          int[] iconpos = new int[2];
                                          headMenueIcon01[7].getLocationOnScreen(iconpos);

                                          fileBrowser.createList("TrashRecoverList",1, "",6,
                                                  iconpos[0] +(int)(headMenueIcon01[7].getWidth() +10),iconpos[1] -(int)(headMenueIcon01[7].getHeight() +10),
                                                  (int)(displayWidth/f),"ru");

                                          showListLayout.bringToFront();
                                      }

                                  } else if(caller.equals("TrashRecoverList")) {
                                      int p = Integer.parseInt(tag.substring(0, tag.indexOf(" ")));
                                      commandString = fileBrowser.context.getFilesDir() +"/.TrashIndex/"+ arrayList.get(p)[0];
                                      kind_of = "Recover Trash";
                                      //new FileBrowser.dataHandler("move",  commandString, arrayList.get(p)[1],null).start();
                                      fileBrowser.startTerminalCommands("mvcp -r ",commandString, arrayList.get(p)[1] +"/"+ arrayList.get(p)[0]);

                                      devicePath = arrayList.get(p)[1];
                                      arrayList.remove(p);
                                      fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                  }
                                  else if(caller.equals("mailList")) {
                                      fileBrowser.selectedTx02 = Integer.parseInt(tag.substring(0,tag.indexOf(" ")));
                                      for(ImageView fImg : folderImg)
                                          fImg.setImageBitmap((fileBrowser.bitmapLoader("Icons/mailIcons/AAfile_closed.png")));

                                      if(v.getTag().toString().contains(tag)) {
                                          fileBrowser.selectedTx02 = -1;
                                                  v.setTag(v.getTag().toString().replace("open", "closed"));
                                          ((TextView) v).setTextColor(txColor);
                                          fileBrowser.createSendEmail.mainRel.removeView(fileBrowser.createSendEmail.folderInxScr);
                                          ((LinearLayout)fileBrowser.createSendEmail.mainRel.getChildAt(0)).removeView(fileBrowser.createSendEmail.TextLin);
                                      } else {
                                          folderImg[Integer.parseInt(tag.substring(0, tag.indexOf(" ")))].setImageBitmap(
                                                  ((fileBrowser.bitmapLoader("Icons/mailIcons/AAfile_open.png"))));
                                          fileBrowser.createSendEmail.mainRel.removeView(fileBrowser.createSendEmail.folderInxScr);
                                          fileBrowser.createSendEmail.showFolderIndex(tag.substring(tag.indexOf(" ") + 1, tag.lastIndexOf("_")));
                                          fileBrowser.changeIcon(fileBrowser.createSendEmail.icons[2], "mailIcons","open","closed");
                                      }
                                  } else if(caller.equals("mailCallAccountList") || caller.equals("mailSentAccountList")) {
                                      String kind = caller.substring(4,caller.indexOf("Account"));
                                      nn = Integer.parseInt(tag.substring(0,tag.indexOf(" ")));
                                      int o = 3;
                                      if(caller.contains("Sent"))
                                          o = 6;

                                      fileBrowser.blink = new FileBrowser.blinkIcon(fileBrowser.createSendEmail.icons[o], "Call");
                                      fileBrowser.blink.start();

                                      fileBrowser.createSendEmail.handleSendThread(kind);
                                      if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                                          fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                      }
                                  } else if(caller.equals("mailSaveList")) {
                                      fileBrowser.blink = new FileBrowser.blinkIcon(fileBrowser.createSendEmail.icons[fileBrowser.createSendEmail.icons.length -4], "Save");
                                      fileBrowser.blink.start();

                                      if(fileBrowser.createSendEmail.saveAddressant(fileBrowser.createSendEmail.memoryList[1]))
                                         fileBrowser.createSendEmail.saveEmail(tag.substring(tag.indexOf(" ")+1, tag.lastIndexOf("_")));

                                  } else if(caller.equals("mailAddList")) {
                                      fileBrowser.createSendEmail.deleteIndividium = "ToAddresses" +"/"+ ((TextView) v).getText().toString().trim();
                                      if(fileBrowser.createSendEmail.headerEdit[1].getText().length() > 0)
                                          fileBrowser.createSendEmail.headerEdit[1].setText(fileBrowser.createSendEmail.headerEdit[1].getText().toString() + ", " +
                                                  ((TextView) v).getText().toString().trim());
                                      else
                                          fileBrowser.createSendEmail.headerEdit[1].setText(((TextView) v).getText().toString().trim());
                                      fileBrowser.createSendEmail.memoryList[1] = fileBrowser.createSendEmail.headerEdit[1].getText().toString().trim();
                                  } else if(caller.equals("TextList")) {
                                      if(tag.contains("Logo")) {
                                          fileBrowser.createTxEditor.noAddr = false;
                                          fileBrowser.createTxEditor.isLogo = true;
                                          fileBrowser.createTxEditor.isBackground = false;
                                          fileBrowser.createTxEditor.action = "text + AddressLogo";
                                          if (devicePath.endsWith(".png"))
                                              logoPath = devicePath;
                                          else if (logoPath.length() > 0 && !logoPath.endsWith(".png"))
                                              logoPath = logoPath + ".png";
                                          else if (logoPath.length() == 0) {
                                              fileBrowser.messageStarter("Instruction_LogoAccount", docu_Loader("Language/" + language + "/Instruction_LogoAccount.txt"),  8000);
                                              return;
                                          }

                                          fileBrowser.createTxEditor.createTextEditorDisplay(fileBrowser.createTxEditor.mainLin);

                                      } else if(tag.contains("AbsAddress")) {
                                          fileBrowser.createTxEditor.noAddr = false;
                                          fileBrowser.createTxEditor.isLogo = false;
                                          fileBrowser.createTxEditor.isBackground = false;
                                          if(fileBrowser.createTxEditor.accountAddrData != null && fileBrowser.createTxEditor.accountAddrData.length > 0 &&
                                                  !fileBrowser.createTxEditor.accountAddrData[4].contains("(!"))  {
                                              fileBrowser.createTxEditor.action = "text + Address";
                                              fileBrowser.createTxEditor.createTextEditorDisplay(fileBrowser.createTxEditor.mainLin);
                                          } else {
                                              fileBrowser.messageStarter("Instruction_LogoAccount", docu_Loader("Language/" + language + "/Instruction_EditorAccount.txt"),  8000);
                                              return;
                                          }
                                      } else if(tag.contains("Background")) {
                                          fileBrowser.createTxEditor.isLogo = false;
                                          fileBrowser.createTxEditor.isBackground = true;
                                          fileBrowser.createTxEditor.noAddr = true;
                                          if(devicePath.substring(devicePath.lastIndexOf("/") +1).contains(".png") || devicePath.substring(devicePath.lastIndexOf("/") +1).contains(".jpg")) {
                                              fileBrowser.createTxEditor.isBackground = true;
                                              fileBrowser.createTxEditor.isBackgroundPath = devicePath;
                                              fileBrowser.createTxEditor.createTextEditorDisplay(fileBrowser.createTxEditor.mainLin);
                                          } else {
                                              fileBrowser.messageStarter("Instruction_BackgroundPicture", docu_Loader("Language/" + language + "/Instruction_BackgroundPicture.txt"), 8000);
                                              return;
                                          }

                                      } else if(tag.contains("Text")) {
                                          fileBrowser.createTxEditor.isLogo = false;
                                          fileBrowser.createTxEditor.isBackground = false;
                                          fileBrowser.createTxEditor.noAddr = true;
                                          fileBrowser.createTxEditor.action = "text";
                                          fileBrowser.createTxEditor.isBackgroundPath = devicePath;

                                          fileBrowser.createTxEditor.mainTx="";
                                          fileBrowser.createTxEditor.createTextEditorDisplay(fileBrowser.createTxEditor.mainLin);
                                      }

                                  } else if(caller.equals("PdfSideList")) {
                                      int pNr = Integer.parseInt(tag.substring(tag.lastIndexOf(" ")+1, tag.lastIndexOf("_"))) -1;
                                      selectedTx_01 = pNr;
                                      scrPosY = gridScr.getScrollY();
                                      try {
                                          fileBrowser.createTxEditor.imgView.setImageBitmap(fileBrowser.createTxEditor.openPdf(pNr, new File(devicePath)));
                                      } catch(IOException io){}
                                      showListLayout.bringToFront();
                                  } else if(caller.equals("PdfCombineList")) {
                                      int sz = 2;
                                      if(yfact < 0.625)
                                          sz = 3;
                                      if(tag.substring(tag.indexOf(" ") +1).startsWith("PDF ")) {
                                          if (!devicePath.equals("") && !devicePath.substring(devicePath.lastIndexOf("/") + 1).contains("."))
                                              fileBrowser.messageStarter("pdfCombinedDocument_Save",
                                                      docu_Loader("Language/" + language + "/" + "PdfCombinedDocument_Save" + ".txt"),  0);
                                          else
                                              fileBrowser.messageStarter("Instruction_TxDocumentSave", docu_Loader("Language/" + language + "/Instruction_PdfCombination.txt"),  8000);
                                      } else if(tag.substring(tag.indexOf(" ") +1).contains("import")) {
                                          if (!devicePath.equals("") && (devicePath.substring(devicePath.lastIndexOf("/") + 1).contains(".") &&
                                                  !(devicePath.substring(devicePath.lastIndexOf("/") + 1).contains(".pdf") || devicePath.substring(devicePath.lastIndexOf("/") + 1).contains(".txt")))) {
                                              fileBrowser.createTxEditor.importImg = true;

                                              fileBrowser.createTxEditor.createPdfEditorDisplay(fileBrowser.createTxEditor.mainLin);
                                          } else {
                                              fileBrowser.messageStarter("Instruction_TxDocumentSave", docu_Loader("Language/" + language + "/Instruction_ImageImport.txt"), 8000);
                                          }
                                      } else if(tag.substring(tag.indexOf(" ") +1).startsWith("Text ")) {

                                             if(fileBrowser.createTxEditor != null & fileBrowser.createTxEditor.isVisible()) {
                                                 fileBrowser.createTxEditor.isBackground = true;
                                                 fileBrowser.createTxEditor.isBackgroundPath = devicePath;
                                                 fileBrowser.createTxEditor.createTextEditorDisplay(fileBrowser.createTxEditor.mainLin);
                                             }

                                      }

                                  } else if(caller.equals("PdfSaveList")) {
                                      if (!devicePath.substring(devicePath.lastIndexOf("/") + 1).contains(".") ||
                                           fileBrowser.createTxEditor.loadedFile.endsWith(".pdf")) {
                                          File file = new File(devicePath);
                                          boolean canWrite = file.canWrite();
                                          if (!canWrite) {
                                              fileBrowser.messageStarter("Instruction_Save_Not_Possible", docu_Loader("Language/" + language + "/Save_Not_Possible.txt"), 8000);
                                          return;
                                          }
                                          isPdf = false;
                                          String kind = "TxDocument_Save";
                                          if (tag.contains("PDF ")) {
                                              kind = "PDFDocument_Save";
                                              if(fileBrowser.createTxEditor != null && fileBrowser.createTxEditor.isVisible())
                                                  if(fileBrowser.createTxEditor.loadedFile.endsWith(".pdf"))
                                                      kind = "PdfDocument_Save";

                                          }

                                          fileBrowser.messageStarter(kind,
                                                  docu_Loader("Language/" + language + "/"+kind+".txt"), 0);
                                      } else {
                                          fileBrowser.messageStarter("Instruction_TxDocumentSave", docu_Loader("Language/" + language + "/Instruction_TxDokumentSave.txt"), 8000);
                                      }
                                  }
                                  else if(caller.equals("documentList")) {
                                      String kind = "textEditorDisplay", format = ".txt";
                                      if(tag.contains("PDF")) {
                                          kind = "pdfEditorDisplay";
                                          format = ".pdf";
                                      }
                                      Bundle bund = new Bundle();
                                      bund.putString("CALLER", kind +"New");
                                      bund.putString("FORMAT",format);
                                      bund.putStringArray("TEXT", new String[0]);

                                      fileBrowser.fragmentStart(fileBrowser.createTxEditor, 7,kind, bund, 0, 0,
                                              displayWidth, displayHeight);
                                      fileBrowser.fragmentShutdown(fileBrowser.showList,3);
                                  } else if(caller.equals("searchMachineList")) {
                                      int nr = Integer.parseInt(tag.substring(0,tag.indexOf(" ")));
                                      fileBrowser.searchMashineUrl = tag.substring(tag.indexOf(" ")+1);
                                      fileBrowser.changeIcon(headMenueIcon[7],"headMenueIcons","open","closed");
                                      fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                                  } else if(caller.equals("webSideMemoryList")) {
                                      ImageView trash = new ImageView(fileBrowser);
                                      trash.setLayoutParams(new RelativeLayout.LayoutParams(showListLayout.getWidth()/5, showListLayout.getWidth()/5));
                                      trash.setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/Trash.png"));
                                      trash.setTag(v.getTag().toString().substring(v.getTag().toString().indexOf(" ") +1,
                                              v.getTag().toString().lastIndexOf("_")));
                                      trash.setX((float)(showListLayout.getWidth() -showListLayout.getWidth()/6));
                                      trash.setY(-showListLayout.getWidth()/28);

                                      trash.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              int c = 0;
                                              String tag1 = view.getTag().toString();
                                              String[] webSideMem = fileBrowser.read_writeFileOnInternalStorage("read", "WebSideMemory", "WebSideMemory_Saved.txt", ""),
                                                        ebTrans = new String[0];
                                              String trans = "";
                                              for (String s : webSideMem)
                                                  if (!s.equals(tag1)) {
                                                      ebTrans = Arrays.copyOf(ebTrans,ebTrans.length +1);
                                                      ebTrans[ebTrans.length -1] = s;
                                                      trans = trans + s + "\n";
                                                  }
                                              if(!trans.equals(""))
                                                  trans = trans.substring(0, trans.lastIndexOf("\n"));

                                              if (fileBrowser.read_writeFileOnInternalStorage("write", "WebSideMemory", "WebSideMemory_Saved.txt", trans).length == 0) {
                                                  arrayList = new ArrayList<>();
                                                  for (String s : ebTrans) {
                                                      arrayList.add(new String[]{s});
                                                  }
                                                  gridScr.removeAllViews();
                                                  gridScr.addView(createList());
                                              }
                                              if(trans.equals("")) {
                                                  showListLayout.removeAllViews();
                                                  fileBrowser.changeIcon(fileBrowser.webBrowserDisplay.steerImgs[3],"browserIcons","open","closed");
                                                  fileBrowser.fragmentShutdown(fileBrowser.showList,3);
                                              }
                                          }
                                      });
                                      showListLayout.addView(trash);
                                  }
                              }
                          });
                      } else {
                          if(caller.equals("startListUrl") && (""+(float)i1/2).endsWith(".5")) {
                              listTx[listTx.length - 1].setTextSize((float) (4 * textSize / 5));
                              if(!arrayList.get(i)[i1].equals("no found")) {
                                  File file = new File(arrayList.get(i)[i1]);
                                  boolean canWrite = file.canWrite();
                                  if (canWrite)
                                      listTx[listTx.length - 1].setText("allowed ->\nread&write");
                                  else
                                      listTx[listTx.length - 1].setText("allowed ->\nread");
                                  listTx[listTx.length - 1].setTag(arrayList.get(i)[i1]);
                              } else {
                                  String nofound = "not found";
                                  if(language.equals("Deutsch"))
                                      nofound = "nicht gefunden";

                                  listTx[listTx.length - 1].setText(nofound);
                                  listTx[listTx.length - 1].setTag("");
                                  listTx[listTx.length - 2].setEnabled(false);
                                  listTx[listTx.length - 2].setTextColor(getResources().getColor(R.color.grey));
                              }
                          }
                      }
                      if(caller.equals("mailList")) {
                          folderImg = Arrays.copyOf(folderImg, folderImg.length+1);
                          folderImg[folderImg.length -1] = new ImageView(fileBrowser);
                          folderImg[folderImg.length -1].setLayoutParams(new RelativeLayout.LayoutParams(showListLayout.getWidth()/4, showListLayout.getWidth()/4));
                          folderImg[folderImg.length -1].setImageBitmap((fileBrowser.bitmapLoader("Icons/mailIcons/AAfile_closed.png")));
                          if(fileBrowser.selectedTx02 != -1 && folderImg.length -1 == fileBrowser.selectedTx02)
                              folderImg[folderImg.length -1].setImageBitmap((fileBrowser.bitmapLoader("Icons/mailIcons/AAfile_open.png")));

                          maingrid.addView(folderImg[folderImg.length -1]);
                      }
                      maingrid.addView(listTx[listTx.length - 1]);
                  }
              }
              return maingrid;
    }
}