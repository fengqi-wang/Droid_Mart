import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

final public class MediaService extends Service {

    private String TAG = "MediaService";
    // TODO Move data inside this class to avoid data missing after the activity is killed
    private WebServer webServer = null;

    private final String POSITION= "/position";
    private final String PING= "/ping";
    private final String CHECK= "/check";
    private final String COUNT= "/count";
    private final String TYPES= "/types";

    private static Service instance = null;
    public static Service getInstance(){
        return instance;
    }

    //boolean isFromTServer = false;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      //  if(intent != null){
        //    isFromTServer = 1 == intent.getIntExtra(SharedConstants.EXTRA_START_SERVICE, 0);
        //}
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        DeviceManagerServer.getInstance().setMyMac(WIFITools.getMacAddress(this));
        DeviceManagerServer.getInstance().setMyIp(WIFITools.getIPAddress(this));
        instance = this;
        webServer = new WebServer();
        try {
            webServer.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        MobclickAgent.onResume(MediaService.this);
        MobclickAgent.onPageStart("MediaService");
    }

    @Override
    public void onDestroy() {
        webServer.stop();
        MobclickAgent.onPause(MediaService.this);
        MobclickAgent.onPageEnd("MediaService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(8080);
        }

        @Override
        public Response serve(IHTTPSession session) {
            NanoHTTPD.Response res = null;

            String uri = session.getUri();
            if(uri.indexOf(POSITION) != -1){
                DeviceInfoServer deviceInfoServer = getAuthDevice(session);
                if(deviceInfoServer == null || !deviceInfoServer.getAllowed()) {
                    return getAuthErrorResponse();
                }
                int size = ImageManager.getInstance().imagePathMap.size();
                if(size ==0)
                {
                    return getNotfoundResponse();
                }
                String posStr = uri.substring(POSITION.length() + 1);

                MediaInfo mediaInfo  = ImageManager.getInstance().imagePathMap.get(posStr);
                if(mediaInfo == null){
                    return getNotfoundResponse();
                }
                res = getImageResponse(mediaInfo, deviceInfoServer);
                //MobclickAgent.onEvent(MediaService.this, "position", posStr + "=" + mediaInfo.path);
            }
            else if(uri.equalsIgnoreCase(TYPES)){
                DeviceInfoServer deviceInfoServer = getAuthDevice(session);
                if(deviceInfoServer == null || !deviceInfoServer.getAllowed()) {
                    return getAuthErrorResponse();
                }

                String countStr = "";
                int n = 0;
                if(ImageManager.getInstance().imagePathMap.size() > 0){
                    String s = ImageManager.getInstance().getTypeList();
                    countStr = s;
                }
                InputStream stream = new ByteArrayInputStream(countStr.getBytes());
                res = new Response(Response.Status.OK,
                        "text/plain", stream, countStr.getBytes().length);
                //MobclickAgent.onEvent(MediaService.this, "types", "types");

            }
            else if(uri.equalsIgnoreCase(COUNT)){
                DeviceInfoServer deviceInfoServer = getAuthDevice(session);
                if(deviceInfoServer == null || !deviceInfoServer.getAllowed()) {
                    return getAuthErrorResponse();
                }
                String countStr = "";
                int n = 0;
                if(ImageManager.getInstance().imagePathMap.size() > 0){
                    Object [] hashs = ImageManager.getInstance().imagePathMap.keySet().toArray();
                    String str = Arrays.toString(hashs);
                    countStr = str.toString().substring(1, str.length() - 1);
                    n = hashs.length;
                }
                InputStream stream = new ByteArrayInputStream(countStr.getBytes());
                res = new Response(Response.Status.OK,
                        "text/plain", stream, countStr.getBytes().length);
                //MobclickAgent.onEvent(MediaService.this, "count", "" + n);

            }
            else if(uri.equalsIgnoreCase(PING)){
                if(!checkPingAuth(session)){
                    return getAuthErrorResponse();
                }

                JSONObject obj = new JSONObject();

                try {
                    obj.put(SharedConstants.IP, WIFITools.getIPAddress(MediaService.this));
                    String deviceName = ShareTools.getDeviceName();
                    try {
                        deviceName = URLEncoder.encode(deviceName, SharedConstants.UTF8);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    obj.put(SharedConstants.DEVICE, deviceName);
                    obj.put(SharedConstants.MAC, WIFITools.getMacAddress(MediaService.this));
                    String showName = DeviceManagerServer.getInstance().getShowName(getApplication());
                    if(showName != null && !showName.isEmpty()) {
                        try {
                            showName = URLEncoder.encode(showName, SharedConstants.UTF8);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(showName != null && !showName.isEmpty()) {
                        obj.put(SharedConstants.SHOW_NAME, showName);
                    }

                    PingObject pingObject = ImageManager.getInstance().getPingObject();
                    obj.put(SharedConstants.HASH, pingObject.hash);
                    obj.put(SharedConstants.GIF_STRING, pingObject.gif);
                    obj.put(SharedConstants.IMAGE, pingObject.image);
                    obj.put(SharedConstants.VIDEO, pingObject.video);
//                    if(isFromTServer){
//                        obj.put(SharedConstants.EXTRA_START_SERVICE, "1");
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String str = obj.toString();
                InputStream stream = new ByteArrayInputStream(str.getBytes());
                res = new Response(Response.Status.OK,
                        "text/plain", stream, str.getBytes().length);
                //MobclickAgent.onEvent(MediaService.this, "ping", str);
            }
            else if(uri.equalsIgnoreCase(CHECK)){
                String str = ImageManager.getInstance().getHashForCollection();
                InputStream stream = new ByteArrayInputStream(str.getBytes());
                res = new Response(Response.Status.OK,
                        "text/plain", stream, str.getBytes().length);
                //MobclickAgent.onEvent(MediaService.this, "check_change", str);
            }

            return res;
        }


        private Response getAuthErrorResponse(){
            String countStr = "";
            InputStream stream = new ByteArrayInputStream(countStr.getBytes());
            Response res = new Response(Response.Status.UNAUTHORIZED,
                    "text/plain", stream, countStr.getBytes().length);

            return res;
        }

        private Response getNotfoundResponse(){
            String countStr = "";
            InputStream stream = new ByteArrayInputStream(countStr.getBytes());
            Response res = new Response(Response.Status.NOT_FOUND,
                    "text/plain", stream, countStr.getBytes().length);

            return res;
        }

        private final String contentType = "image";
        private Response getImageResponse(MediaInfo mediaInfo, DeviceInfoServer deviceInfoServer) {
            String path = mediaInfo.path;
            int type = mediaInfo.mediaType;
            FileInputStream fis = null;
            Response res = null;
            try {
                File mediaFile = new File(path);
                fis = new FileInputStream(mediaFile);
                res = new Response(Response.Status.OK,
                                contentType, fis, mediaFile.length());
                res.addHeader(SharedConstants.MEDIA_TYPE, type + "");

                String filename;
                try {
                    filename = path.substring(path.lastIndexOf("/") + 1);
                } catch (Exception e) {
                    filename = path;
                }
                try {
                    filename = URLEncoder.encode(filename, SharedConstants.UTF8);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                res.addHeader(SharedConstants.MEDIA_NAME, filename);
                res.addHeader(SharedConstants.MEDIA_SHOW_WATERMARK, deviceInfoServer.getShowWatermark() ? "1" : "0");
                res.addHeader(SharedConstants.MEDIA_ALLOW_LONG_PRESS_DOWNLOAD, deviceInfoServer.getAllowLongPressDownload() ? "1" : "0");

                String showName = DeviceManagerServer.getInstance().getShowName(MediaService.this);
                try {
                    showName = URLEncoder.encode(showName, SharedConstants.UTF8);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                res.addHeader(SharedConstants.MEDIA_SERVER_SHOW_NAME, showName);

            } catch (Exception e) {
                e.printStackTrace();
                return getNotfoundResponse();
            }
            return res;
        }
    }

}
