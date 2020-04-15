
import cc.plural.jsonij.JSON;
import cc.plural.jsonij.parser.ParserException;
import com.mumu.msg.ADDGroupExample;
import com.mumu.msg.AddAdmin;
import com.mumu.msg.AddFriendExample;
import com.mumu.msg.AddFriends;
import com.mumu.msg.DeleteAdmin;
import com.mumu.msg.RE_MSG_AdminChange;
import com.mumu.msg.RE_MSG_Forum;
import com.mumu.msg.RE_MSG_Group;
import com.mumu.msg.RE_MSG_Private;
import java.io.IOException;
import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class KQWebClient
  extends WebSocketClient
{
  QQrobot adapter = null;//用自己写的QQrobot扩展功能
  
  public KQWebClient(URI serverURI)
  {
    super(serverURI);
    connect();
  }
  
  public void onClose(int arg0, String arg1, boolean arg2)
  {
    System.out.println("服务器已经关闭");
  }
  
  public void onError(Exception e)
  {
	  e.printStackTrace();
    System.out.println("发生未知错误");
    
    this.sendPrivateMSG(adapter.owner, "发生异常！\\n"+e.toString());
    for(StackTraceElement s:e.getStackTrace()) {
    	this.sendPrivateMSG(adapter.owner, s.toString());
    }
    
  }
  
  public void onMessage(String msg)
  {
    if (this.adapter != null)
    {
      int type = 0;
      JSON json = null;
      try
      {
        json = JSON.parse(msg);
      }
      catch (ParserException e)
      {
        e.printStackTrace();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      type = Integer.parseInt(String.format("%s", new Object[] { json.get("act") }));
      switch (type)
      {
      case 21: 
        this.adapter.Re_MSG_Private(new RE_MSG_Private(msg));
        break;
      case 4: 
        this.adapter.RE_MSG_FORUM(new RE_MSG_Forum(msg));
        break;
      case 2: 
        this.adapter.RE_MSG_Group(new RE_MSG_Group(msg));
        break;
      case 101: 
        this.adapter.RE_EXAMPLE_MANAGE(new RE_MSG_AdminChange(msg));
        break;
      case 102: 
        this.adapter.RE_EXAMPLE_DEMBER(new DeleteAdmin(msg));
        break;
      case 103: 
        this.adapter.RE_EXAMPLE_AMEMBER(new AddAdmin(msg));
        break;
      case 201: 
        this.adapter.RE_EXAMPLE_ADDFRIEND(new AddFriendExample(msg));
        break;
      case 301: 
        this.adapter.RE_ASK_ADDFRIEND(new AddFriends(msg));
        break;
      case 302: 
        this.adapter.RE_ASK_ADDGROUP(new ADDGroupExample(msg));
        break;
      case 0://群员信息
    	this.adapter.checkPermission(Integer.parseInt(String.format("%s", new Object[] { json.get("permission") })),String.format("%s", new Object[] { json.get("QQID") }));
    	  break;
      default: 
        System.out.println(msg);
        
        break;
      }
    }
    else
    {
      System.out.println("监听未添加");
    }
  }
  
  public void onOpen(ServerHandshake arg0)
  {
    System.out.println("服务器连接成功");
    	this.sendPrivateMSG(adapter.owner, "连接成功");
  }
  
  public void addQQMSGListenner(QQrobot adapter)
  {
    this.adapter = adapter;
  }
  
  public void sendPrivateMSG(String qq, String msg)
  {
    try
    {
      Thread.sleep(10L);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    send("{ \"act\": \"106\",\"QQID\":\"" + qq + "\",\"msg\": \"" + msg + "\"" + "}");
  }
  
  public void sendForumMSG(String qq, String forum, String msg, Boolean isAT)
  {
    try
    {
      Thread.sleep(10L);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    if (isAT.booleanValue()) {
      msg = "[CQ:at,qq=" + qq + "]" + msg;
    }
    send("{ \"act\": \"103\",\"discussid\": \"" + forum + "\",\"msg\": \"" + msg + "\"" + "}");
  }
  
  public void sendGroupMSG(String qq, String groupid, String msg, Boolean isAT)
  {
	//扩展内容，确保自己说话后打断复读（重点及细节！）
	  if(adapter.contentOfLastReread.getOrDefault(groupid, "").equals(msg)) {
	  	adapter.hasReread.put(groupid, true);
	  	adapter.addDate(msg, groupid);
	  }else {
		  adapter.hasReread.put(groupid, false);
		  adapter.contentOfLastReread.put(groupid, msg);
	  }
    try
    {
      Thread.sleep(10L);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    if (isAT.booleanValue()) {
      msg = "[CQ:at,qq=" + qq + "] " + msg;
    }
    send("{ \"act\": \"101\",\"groupid\": \"" + groupid + "\",\"msg\": \"" + msg + "\"" + "}");
  }
  
  public void sendPraise(String qq)
  {
    try
    {
      Thread.sleep(10L);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    send("{\"act\": \"110\",\"QQID\": \"" + qq + "\"}");
  }
  
  //------------------分割线以下为补充接口---------------------------
  public void setForbiddenWords(String qq,String groupid,String duration) {
	  send("{\"act\": \"121\",\"QQID\": \"" + qq + "\",\"groupid\":\""
			  +groupid+"\",\"duration\":\""+duration+"\"}");
  }
  
  public void setGroupSpecialtitle(String groupid,String qq,String duration,String newspecialtitle) {
	  send("{ \"act\": \"128\",\"groupid\": \"" + groupid + "\",\"QQID\": \"" + qq
			  + "\",\"duration\": \"" + duration + "\",\"newspecialtitle\": \""+ newspecialtitle + "\""+"}");
  }
  
  public void setGroupNick(String groupid,String QQID,String newcard) {
	  send("{\"act\": \"126\",\"groupid\": \"" + groupid + "\",\"QQID\":\""
			  +QQID+"\",\"newcard\":\""+newcard+"\"}");
  }
  
  public void CheckPermission(String groupid,String QQID,String nocache) {
	  send("{\"act\": \"25303\",\"groupid\": \"" + groupid + "\",\"QQID\":\""
			  +QQID+"\",\"nocache\":\""+nocache+"\"}");
  }
  
  public void agreeAskGroup(String requesttype,String responseoperation,String reason,String responseflag) {
	  send("{\"act\": \"151\",\"requesttype\": \"" + requesttype + "\",\"responseoperation\":\""
			  +responseoperation+"\",\"reason\":\""+reason+"\",\"responseflag\":\""+responseflag+"\"}");
  }
  
}
