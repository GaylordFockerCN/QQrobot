import com.mumu.msg.ADDGroupExample;
import com.mumu.msg.AddAdmin;
import com.mumu.msg.AddFriendExample;
import com.mumu.msg.AddFriends;
import com.mumu.msg.DeleteAdmin;
import com.mumu.msg.RE_MSG_AdminChange;
import com.mumu.msg.RE_MSG_Forum;
import com.mumu.msg.RE_MSG_Group;
import com.mumu.msg.RE_MSG_Private;
import com.mumu.listenner.KQMSGListenner;

public class KQMSGAdapter
  extends KQMSGListenner
{
  public void Re_MSG_Private(RE_MSG_Private msg) {}
  
  public void RE_MSG_FORUM(RE_MSG_Forum msg) {}
  
  public void RE_MSG_Group(RE_MSG_Group msg) {}
  
  public void RE_EXAMPLE_MANAGE(RE_MSG_AdminChange msg)
  {
    System.out.println("���������� ");
  }
  
  public void RE_EXAMPLE_DEMBER(DeleteAdmin msg)
  {
    System.out.println("����������");
  }
  
  public void RE_EXAMPLE_AMEMBER(AddAdmin msg)
  {
    System.out.println("����������");
  }
  
  public void RE_EXAMPLE_ADDFRIEND(AddFriendExample msg)
  {
    System.out.println("������������");
  }
  
  public void RE_ASK_ADDFRIEND(AddFriends msg)
  {
    System.out.println("������������");
  }
  
  public void RE_ASK_ADDGROUP(ADDGroupExample msg)
  {
    System.out.println("����������");
  }
  
  public void checkPermission(int permission,String qqid) {
	  
  }
}
