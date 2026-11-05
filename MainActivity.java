package surf.dolphy;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.net.wifi.WifiManager;
import android.nfc.*;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.NdefRecord;
import android.nfc.NdefMessage;
import android.hardware.usb.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
  final int IMPORT_PLUGIN=10, IMPORT_PROFILE=11, EXPORT_PROFILE=12, PERMS=13;
  LinearLayout page, tiles, toolbar; ScrollView scroller; TextView log;
  final ArrayList<JSONObject> plugins=new ArrayList<>();
  final String[] palette={"#8A5CFF","#00D1BA","#FF8D35","#E94E9A","#72A7FF"};
  int accent=Color.parseColor(palette[0]); boolean editing=false; String newNfcText="";
  NfcAdapter nfc; BluetoothLeScanner ble; ScanCallback scanCallback; Handler handler=new Handler();
  final String[] builtIn={"Bluetooth","Wi-Fi","NFC","USB","Device","Plugins","Appearance","Profiles"};
  int bg=Color.rgb(12,13,20), panel=Color.rgb(26,28,40);
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);nfc=NfcAdapter.getDefaultAdapter(this);load();draw();}
  int dp(int v){return (int)(v*getResources().getDisplayMetrics().density);}
  GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  TextView txt(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(dp(10),dp(10),dp(10),dp(10));if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
  void addBtn(LinearLayout parent,String title,Runnable onClick){TextView t=txt(title,15,false);t.setGravity(Gravity.CENTER);t.setBackground(shape(panel,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.setMargins(0,dp(4),0,dp(4));parent.addView(t,lp);t.setOnClickListener(v->onClick.run());}
  void header(String name){page.addView(txt(name,24,true));}
  void message(String text){new AlertDialog.Builder(this).setMessage(text).setPositiveButton("OK",null).show();}
  void draw(){
    page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(17),dp(12),dp(17),dp(28));page.setBackgroundColor(bg);
    scroller=new ScrollView(this);scroller.setFillViewport(true);scroller.addView(page);setContentView(scroller);
    TextView title=txt("◈ dolphy.surf",29,true);title.setTextColor(accent);page.addView(title);
    TextView sub=txt("YOUR DEVICE • YOUR INTERFACE",11,true);sub.setTextColor(Color.LTGRAY);page.addView(sub);
    toolbar=new LinearLayout(this);toolbar.setOrientation(LinearLayout.HORIZONTAL);page.addView(toolbar);
    TextView edit=txt(editing?"✓ Done":"✎ Edit dashboard",14,true);edit.setTextColor(accent);toolbar.addView(edit);edit.setOnClickListener(v->{editing=!editing;draw();});
    TextView create=txt("＋ Create tile",14,true);toolbar.addView(create);create.setOnClickListener(v->builder());
    TextView note=txt(editing?"Use arrows to arrange your dashboard tiles.":"Long-press a tile to enter edit mode.",12,false);note.setTextColor(Color.GRAY);page.addView(note);
    tiles=new LinearLayout(this);tiles.setOrientation(1);page.addView(tiles);
    for(int i=0;i<plugins.size();i++)renderTile(i);
    TextView foot=txt("Local-first • No data collection • Permission-based tools",11,false);foot.setTextColor(Color.GRAY);page.addView(foot);
  }
  String val(JSONObject o,String k,String fallback){return o.optString(k,fallback);}
  void renderTile(int i){JSONObject o=plugins.get(i);String name=val(o,"name","Tile"), icon=val(o,"icon","◉"), type=val(o,"type","info");
    LinearLayout wrap=new LinearLayout(this);wrap.setOrientation(1);wrap.setPadding(dp(10),dp(8),dp(10),dp(8));wrap.setBackground(shape(panel,16));LinearLayout.LayoutParams ml=new LinearLayout.LayoutParams(-1,-2);ml.setMargins(0,dp(6),0,dp(6));tiles.addView(wrap,ml);
    TextView top=txt(icon+"  "+name,19,true);wrap.addView(top);top.setOnClickListener(v->{if(editing)editTile(i);else open(o);});top.setOnLongClickListener(v->{editing=true;draw();return true;});
    if(editing){LinearLayout controls=new LinearLayout(this);wrap.addView(controls);
      String[] names={"↑","↓","✎","×"};for(int a=0;a<4;a++){final int action=a;TextView b=txt(names[a],17,true);b.setTextColor(accent);controls.addView(b,new LinearLayout.LayoutParams(0,dp(43),1));b.setOnClickListener(v->{if(action==0&&i>0)Collections.swap(plugins,i,i-1);if(action==1&&i<plugins.size()-1)Collections.swap(plugins,i,i+1);if(action==2){editTile(i);return;}if(action==3)plugins.remove(i);save();draw();});}}
  }
  void open(JSONObject o){String type=val(o,"type","info");switch(type){
    case "bluetooth":bluetoothPage();break;case "wifi":wifiPage();break;case "nfc":nfcPage();break;case "usb":usbPage();break;case "device":devicePage();break;case "plugins":pluginsPage();break;case "appearance":appearancePage();break;case "profiles":profilePage();break;
    case "note":showPage(val(o,"name","Note"));page.addView(txt(val(o,"content",""),16,false));break;
    default:message("This plugin type is not supported by this build. No unknown plugin code was executed.");}}
  void showPage(String name){page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(17),dp(12),dp(17),dp(28));page.setBackgroundColor(bg);ScrollView s=new ScrollView(this);s.addView(page);setContentView(s);TextView back=txt("← Dashboard",16,true);back.setTextColor(accent);page.addView(back);back.setOnClickListener(v->draw());header(name);}
  void info(String text){TextView t=txt(text,14,false);t.setTextColor(Color.LTGRAY);page.addView(t);}
  boolean allowed(String permission){return Build.VERSION.SDK_INT<31||checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED;}
  void requestBluetooth(){if(Build.VERSION.SDK_INT>=31)requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT},PERMS);}
  void bluetoothPage(){showPage("Bluetooth Lab");info("Discover nearby BLE devices and list paired devices. Scans use Android's official APIs. No repeated advertising, flooding or interference.");
    if(Build.VERSION.SDK_INT>=31&&!allowed(Manifest.permission.BLUETOOTH_SCAN)){addBtn(page,"Grant Bluetooth permissions",this::requestBluetooth);return;}
    BluetoothManager m=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);BluetoothAdapter a=m==null?null:m.getAdapter();if(a==null){info("Bluetooth hardware unavailable.");return;}
    if(!a.isEnabled()){info("Enable Bluetooth in Android settings.");return;}
    if(!allowed(Manifest.permission.BLUETOOTH_CONNECT)){addBtn(page,"Grant device permissions",this::requestBluetooth);return;}
    addBtn(page,"Show paired devices",()->{StringBuilder sb=new StringBuilder();for(BluetoothDevice d:a.getBondedDevices())sb.append(d.getName()).append(" • ").append(d.getAddress()).append("\n");message(sb.length()==0?"No paired devices":sb.toString());});
    log=txt("Nearby devices appear here.",13,false);page.addView(log);
    addBtn(page,"Scan BLE for 10 seconds",()->{if(!allowed(Manifest.permission.BLUETOOTH_SCAN)){requestBluetooth();return;}ble=a.getBluetoothLeScanner();if(ble==null){message("BLE scanning unavailable");return;}
      if(scanCallback!=null){ble.stopScan(scanCallback);scanCallback=null;}Set<String> seen=new HashSet<>();log.setText("Scanning…");scanCallback=new ScanCallback(){@Override public void onScanResult(int callbackType,ScanResult r){BluetoothDevice d=r.getDevice();String id=d.getAddress();if(seen.add(id))runOnUiThread(()->log.append("\n"+id+"  RSSI "+r.getRssi()+" dBm"));}@Override public void onScanFailed(int error){runOnUiThread(()->log.append("\nScan error: "+error));}};
      try{ble.startScan(scanCallback);handler.postDelayed(()->stopScan(),10000);}catch(SecurityException e){message("Bluetooth permission required");}});
    addBtn(page,"Stop scan",this::stopScan);
  }
  void stopScan(){if(ble!=null&&scanCallback!=null){try{ble.stopScan(scanCallback);}catch(Exception ignored){}scanCallback=null;if(log!=null)log.append("\nScan stopped.");}}
  void wifiPage(){showPage("Wi-Fi Diagnostics");info("Read-only connectivity and link diagnostics. No packets are sent or user information extracted.");WifiManager m=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);if(m==null){info("Wi-Fi unavailable");return;}info("Wi-Fi enabled: "+m.isWifiEnabled()+"\nConnection info: "+String.valueOf(m.getConnectionInfo()));addBtn(page,"Refresh",this::wifiPage);}
  void usbPage(){showPage("USB Devices");UsbManager m=(UsbManager)getSystemService(USB_SERVICE);if(m==null){info("USB host unavailable");return;}Map<String,UsbDevice> devices=m.getDeviceList();if(devices.isEmpty())info("No attached USB devices.");for(UsbDevice d:devices.values())info("Device: "+d.getDeviceName()+"\nVID: "+d.getVendorId()+" PID: "+d.getProductId()+"\nInterfaces: "+d.getInterfaceCount());addBtn(page,"Refresh",this::usbPage);}
  void devicePage(){showPage("Device Details");info("Manufacturer: "+Build.MANUFACTURER+"\nModel: "+Build.MODEL+"\nAndroid: "+Build.VERSION.RELEASE+"\nSDK: "+Build.VERSION.SDK_INT+"\nHardware: "+Build.HARDWARE+"\nSupported ABIs: "+Arrays.toString(Build.SUPPORTED_ABIS));}
  void nfcPage(){showPage("NFC Lab");info("Read supported tags. Write a short NDEF text record to a compatible writable tag you own.");if(nfc==null){info("NFC hardware unavailable.");return;}info("NFC enabled: "+nfc.isEnabled()+"\nHold a tag against your phone with this screen open.");EditText e=new EditText(this);e.setHint("Text to write to your own NDEF tag");e.setTextColor(Color.WHITE);page.addView(e);addBtn(page,"Arm one-time NDEF write",()->{newNfcText=e.getText().toString();if(newNfcText.isEmpty())message("Enter text first");else message("Write armed. Tap your own writable tag to continue. The tag may be overwritten.");});}
  @Override public void onResume(){super.onResume();if(nfc!=null){try{nfc.enableReaderMode(this,tag->runOnUiThread(()->handleTag(tag)),NfcAdapter.FLAG_READER_NFC_A|NfcAdapter.FLAG_READER_NFC_B|NfcAdapter.FLAG_READER_NFC_F|NfcAdapter.FLAG_READER_NFC_V,null);}catch(Exception ignored){}}}
  @Override public void onPause(){if(nfc!=null)try{nfc.disableReaderMode(this);}catch(Exception ignored){}stopScan();super.onPause();}
  void handleTag(Tag tag){String id=bytesToHex(tag.getId());if(newNfcText.isEmpty()){StringBuilder s=new StringBuilder("Tag ID: "+id+"\nTechnologies: "+Arrays.toString(tag.getTechList()));try{Ndef n=Ndef.get(tag);if(n!=null){n.connect();NdefMessage m=n.getNdefMessage();if(m!=null)for(NdefRecord r:m.getRecords())s.append("\nRecord: ").append(r.getTnf()).append(" • ").append(new String(r.getPayload(),StandardCharsets.UTF_8));n.close();}}catch(Exception ex){s.append("\nNDEF read: ").append(ex.getMessage());}message(s.toString());return;}
   String data=newNfcText;newNfcText="";new AlertDialog.Builder(this).setTitle("Confirm tag write").setMessage("Overwrite tag "+id+" with text: "+data+"?").setNegativeButton("Cancel",null).setPositiveButton("Write",(d,w)->new Thread(()->{String result=writeNdef(tag,data);runOnUiThread(()->message(result));}).start()).show();}
  String writeNdef(Tag tag,String value){try{byte[] lang="en".getBytes(StandardCharsets.US_ASCII), body=value.getBytes(StandardCharsets.UTF_8);byte[] payload=new byte[1+lang.length+body.length];payload[0]=(byte)lang.length;System.arraycopy(lang,0,payload,1,lang.length);System.arraycopy(body,0,payload,1+lang.length,body.length);NdefMessage msg=new NdefMessage(new NdefRecord[]{new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],payload)});Ndef n=Ndef.get(tag);if(n!=null){n.connect();if(!n.isWritable()){n.close();return "Tag is read-only";}if(n.getMaxSize()<msg.toByteArray().length){n.close();return "Tag too small";}n.writeNdefMessage(msg);n.close();return "Tag written successfully";}NdefFormatable f=NdefFormatable.get(tag);if(f!=null){f.connect();f.format(msg);f.close();return "Tag formatted and written";}return "Tag does not support NDEF writes";}catch(Exception ex){return "NFC write failed: "+ex.getMessage();}}
  String bytesToHex(byte[] b){StringBuilder sb=new StringBuilder();for(byte x:b)sb.append(String.format("%02X",x));return sb.toString();}
  void pluginsPage(){showPage("Plugin Studio");info("Import JSON .dolphyplugin descriptors. Unknown and executable actions are rejected; arbitrary APK code cannot be made safe by renaming it.");addBtn(page,"Import .dolphyplugin",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT_PLUGIN);});addBtn(page,"Create plugin",this::builder);info("Supported actions: note, bluetooth, wifi, nfc, usb, device, plugins, appearance, profiles.");}
  void builder(){EditText name=new EditText(this);name.setSingleLine(true);name.setHint("Plugin name");name.setTextColor(Color.WHITE);EditText body=new EditText(this);body.setHint("Content or notes");body.setTextColor(Color.WHITE);LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(15),0,dp(15),0);l.addView(name);l.addView(body);new AlertDialog.Builder(this).setTitle("Visual Plugin Builder").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{String s=name.getText().toString().trim();if(s.isEmpty())s="My plugin";JSONObject p=new JSONObject();try{p.put("name",s);p.put("icon","◆");p.put("type","note");p.put("content",body.getText().toString());plugins.add(p);save();draw();}catch(Exception ignored){}}).show();}
  void editTile(int i){JSONObject p=plugins.get(i);EditText name=new EditText(this);name.setText(p.optString("name"));name.setTextColor(Color.WHITE);EditText icon=new EditText(this);icon.setText(p.optString("icon"));icon.setTextColor(Color.WHITE);LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.addView(name);l.addView(icon);new AlertDialog.Builder(this).setTitle("Edit tile name and icon").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{try{p.put("name",name.getText().toString());p.put("icon",icon.getText().toString());save();draw();}catch(Exception ignored){}}).show();}
  void appearancePage(){showPage("Appearance + Firmware Style");info("All changes are saved locally. Theme settings can be exported with profiles.");for(String color:palette){TextView v=txt("●  "+color,18,true);v.setTextColor(Color.parseColor(color));page.addView(v);v.setOnClickListener(x->{accent=Color.parseColor(color);save();appearancePage();});}addBtn(page,"Edit dashboard tiles",()->{editing=true;draw();});}
  void profilePage(){showPage("Local Profiles");info("Export and restore dashboard layouts, plugin descriptors and accent colour. Files stay on your device unless you share them.");addBtn(page,"Export profile JSON",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"dolphy-surf-profile.json");startActivityForResult(i,EXPORT_PROFILE);});addBtn(page,"Import profile JSON",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/json");startActivityForResult(i,IMPORT_PROFILE);});addBtn(page,"Reset dashboard",()->new AlertDialog.Builder(this).setTitle("Reset dashboard?").setNegativeButton("Cancel",null).setPositiveButton("Reset",(d,w)->{defaults();save();draw();}).show());}
  void defaults(){plugins.clear();String[] types={"bluetooth","wifi","nfc","usb","device","plugins","appearance","profiles"};String[] icons={"ᛒ","◉","◇","⛓","▣","⬡","✦","☷"};for(int i=0;i<types.length;i++){JSONObject p=new JSONObject();try{p.put("name",builtIn[i]);p.put("icon",icons[i]);p.put("type",types[i]);plugins.add(p);}catch(Exception ignored){}}}
  JSONObject snapshot(){JSONObject o=new JSONObject();try{o.put("schema",1);o.put("accent",accent);JSONArray a=new JSONArray();for(JSONObject p:plugins)a.put(p);o.put("plugins",a);}catch(Exception ignored){}return o;}
  void save(){getPreferences(MODE_PRIVATE).edit().putString("state",snapshot().toString()).apply();}
  void load(){String s=getPreferences(MODE_PRIVATE).getString("state",null);if(s==null){defaults();return;}try{applyProfile(new JSONObject(s));}catch(Exception e){defaults();}}
  boolean validType(String t){return Arrays.asList("bluetooth","wifi","nfc","usb","device","plugins","appearance","profiles","note").contains(t);}
  void applyProfile(JSONObject o)throws JSONException{JSONArray a=o.getJSONArray("plugins");if(a.length()>100)throw new JSONException("Too many tiles");ArrayList<JSONObject> temp=new ArrayList<>();for(int j=0;j<a.length();j++){JSONObject p=a.getJSONObject(j);String type=p.optString("type","note");if(!validType(type))throw new JSONException("Unsupported action: "+type);if(p.optString("name","").length()>80||p.optString("content","").length()>20000)throw new JSONException("Plugin content too large");temp.add(p);}plugins.clear();plugins.addAll(temp);accent=o.optInt("accent",Color.parseColor(palette[0]));}
  String readText(android.net.Uri u)throws IOException{try(InputStream in=getContentResolver().openInputStream(u);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>1024*1024)throw new IOException("File exceeds 1 MB");}return out.toString("UTF-8");}}
  @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;try{if(request==EXPORT_PROFILE){try(OutputStream o=getContentResolver().openOutputStream(data.getData())){o.write(snapshot().toString(2).getBytes(StandardCharsets.UTF_8));}message("Profile exported");}else if(request==IMPORT_PROFILE){applyProfile(new JSONObject(readText(data.getData())));save();draw();}else if(request==IMPORT_PLUGIN){JSONObject p=new JSONObject(readText(data.getData()));String type=p.optString("type",p.optString("action",""));if(!validType(type))throw new JSONException("Incompatible plugin format/action: "+type);JSONObject safe=new JSONObject();safe.put("name",p.optString("name","Imported Plugin"));safe.put("icon",p.optString("icon","◈"));safe.put("type",type);safe.put("content",p.optString("content",""));if(safe.optString("name").length()>80)throw new JSONException("Name too long");plugins.add(safe);save();draw();message("Plugin imported. Only supported declarative actions are enabled.");}}catch(Exception e){message("Import/export failed: "+e.getMessage());}}
}
