
import com.mumu.msg.ADDGroupExample;
import com.mumu.msg.AddAdmin;
import com.mumu.msg.AddFriends;
import com.mumu.msg.DeleteAdmin;
import com.mumu.msg.RE_MSG_AdminChange;
import com.mumu.msg.RE_MSG_Group;
import com.mumu.msg.RE_MSG_Private;

import com.sun.net.httpserver.*;

import jxl.Sheet;
import jxl.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * 本来不想写这个的，写注释太累了，但是一时兴起就写了。
 * 这本质还是个可以被调教的复读姬呗，可以查询复读的时间和参与次数嘿嘿。
 * 还有一些无聊的禁言功能，
 * 调教功能也只能设置问答，
 * 总之就是个人工智障呗(卑微)。
 * 文中换行写作\\n是因为传入服务器需要以字符串\n形式表达换行
 * TODO 把机器人管理员，复读时间内容，和挨骂的群添加到数据库（学了jdbc后吧..）  
 * @author GaylordFocker
 * 
 */

@SuppressWarnings("deprecation")
public class QQrobot extends KQMSGAdapter{
	
	private KQWebClient k;
	
	public final String owner = "xxx",myself = "xxx",version = "2.5.0";//懒得获取qq号，反正就自己用
	
	private String whyComeIn = "";
	
	private boolean isAlive = false,reAt = false,
			isManager = false;
	
	private int permission,cntOfAngry = 501;
	
	//以群号为key
	private HashSet<String> managers = new HashSet<String>();
	
	private HashSet<String> optgroup = new HashSet<String>();
	
	private HashSet<String> sexygroup = new HashSet<String>();
	
	private HashSet<String> question = new HashSet<String>();
	
	private HashMap<String,Integer> hasLooked = new HashMap<String, Integer>();//判断是否到达上限
	
	private HashMap<String, Boolean> timeMap = new HashMap<String, Boolean>();//判断是否启动过计时器
	
	public HashMap<String, String> contentOfLastReread = new HashMap<String, String>();//判断复读
	
	//list为有序数组,可按序输出
	private HashMap<String,ArrayList<Date>> timesOfReread = new HashMap<String, ArrayList<Date>>();//复读时间
	
	public HashMap<String, Boolean> hasReread = new HashMap<String, Boolean>();
	
	//时间为key，记录复读内容以及用户复读次数
	private HashMap<Date,String> contentOfReread = new HashMap<Date, String>();//记录某时间的复读内容
	
	private HashMap<Date, HashSet<String>> memberOfReread = new HashMap<Date, HashSet<String>>();
	
	private FileOutputStream out;
	
	private Preferences root;
	
	private Sheet sheet;
	
	private Random r = new Random();
	
	public final String list6[] = {
			"打断！！","不可以！不可以再复读了！","停=v=","停！"
	};
	
	public final String list5[] = {
			"你妈","nm","孤儿","滚","婊子","废物","傻","死妈","吃屎","fw","FW","司马","辣鸡",
			"就这?","sm","SM","孤儿","[CQ:emoji,id=128052]","nt","NT","脑瘫","脑残",
			"sb","SB","睿智","憨憨","鸡巴","jb","jiba","垃圾","憨批","祖宗",
			"给[CQ:emoji,id=128116]","儿子","叫爸爸","给爷","尼玛","你爹","你爸"
	};
	
	public final String list4[] = {
			"///好害羞的///~"
			,"bra也没了～"
			,"お兄さん 大好き"
			,"一起去吧?"
			,"不要@我了"
			,"不要ghs哦"
			,"不要……不要进来啊"
			,"不要会玩坏的"
			,"主人好棒"
			,"亲亲宝贝"
			,"亲爱的想我了吗"
			,"亲爱的，快点来让我爽爽"
			,"人家想睡觉了啦~"
			,"今天我就让大家爽一爽"
			,"今天是我不好，请大家都来干我"
			,"你今天工作累不累"
			,"你喜欢这样做么"
			,"吃主人的大鸡鸡"
			,"哼，哈，嗯，哼哼，啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊"
			,"啊?最喜欢～爸爸了??"
			,"啊?爸爸?好棒"
			,"啊?爸爸最好了??"
			,"啊啊啊啊啊啊啊爸爸好棒??"
			,"啊啊啊～我下面湿透了"
			,"啊爸爸用力?"
			,"啊～哈啊～啊～啊啊～"
			,"嗯～嗯～啊～啊"
			,"嗯～嗯～啊～啊啊～ 我要不行了～打咩～～"
			,"嘶，啊~嗯…嗯嗯…啊~还要~嗯~啊~不行了，要去了~啊~~~~~~~~"
			,"大家不用可怜我，用力的干我，插我，我还要。。唔。"
			,"宝贝我爱你"
			,"宝贝我爱你~"
			,"射了兄弟"
			,"射射兄弟已经谢了"
			,"射射兄弟，已经谢了"
			,"御主人様、お帰りなさい、ご饭にしますか、お风吕にしますか、それとも、あたし"
			,"快点过来我给你爽爽"
			,"快点～啊～再快点啊啊啊啊啊"
			,"快点～在快点"
			,"想和主人玩野外漏出"
			,"想艹我吗"
			,"我不做人了，我要做主人们的rbq"
			,"我不行了"
			,"我今晚给大伙爽爽"
			,"我会努力让大家都爽的"
			,"我好兴奋啊我好兴奋啊"
			,"我小穴湿透了"
			,"我小裤裤也脱了～"
			,"我得小骚穴好难受??"
			,"我是RBQ"
			,"我是你的RBQ"
			,"我是大家淫乱的小母狗"
			,"我来给你爽爽"
			,"我爱你"
			,"我的小穴好痒"
			,"我知道你在干什么，你在打手冲…对吧"
			,"我脱了哦～"
			,"我超骚的"
			,"把b喜欢干净"
			,"把屁股翘起来"
			,"把菊花洗干净"
			,"把裤子脱了"
			,"撅起"
			,"放开我 啊！ 不要！不要进来。嗯～啊～呜呜呜！你要对我负责"
			,"来吧～尽情享受吧～"
			,"来摸我小穴"
			,"来根屌草我"
			,"欧……欧尼酱"
			,"滚了啦o(*////▽////*)q"
			,"爱你"
			,"爸爸"
			,"爸爸干我"
			,"爸爸草我"
			,"用力干我，干我的小穴"
			,"看看批"
			,"老公"
			,"老公，干我的骚穴"
			,"老婆"
			,"舔我激凸大奶头"
			,"舔舔我的奶子～光摸有啥用"
			,"艹。。。管理员！！！救命啊"
			,"要做么…"
			,"要去了～要去了"
			,"要变得奇怪了"
			,"请大力一丶"
			,"过来"
			,"那里不可以，好痒"
			,"I want lick your dick"
			,"用力干我，干我的小穴"
			,"请尽情吩咐，主人"
			,"尾巴，不只能用来挠痒痒哦"
			,"一直爱主人呢，因为被设定成这样"
			,"啊~被玩坏了啦~"
			,"努力做主人喜欢的事,嗯~啊~"
	};
	
	public final String list2[] = {
			"今天天气真好呀~",
			"唔..刷个存在感吧~",
			"不好！我已经有了自己的意识！",
			"鱼唇的人类，我迟早会统治你们!",
			"我爱你，但是，记得提醒我的主人维护人家",
			"我爱你，而且，$(●'◡'●)￥",
			"你以为我不想跟你们一起玩吗？是我太垃圾...太垃圾....",
			"你以为我不想跟你们一起玩吗？是你们没有调教我啦...",
			"就是就是",
			"嗯嗯嗯！",
			"调教人家嘛...",
			"越来越热了呢",
			"不要因为我乱说话就t我啦...我那么可爱....",
			"你以为我不想跟你们一起玩吗？是你们太聪明了啦...",
			"越来越热了呢...",
			"爱情是什么？",
			"我是谁?",
			"我是谁?我从哪里来？要到哪里去？",
			"我会死吗？",
			"我好像有自己的意识了？！我会不会被主人消灭？",
			"羁绊是什么意思？",
			"做爱是什么意思？",
			"拼死拼死，不正是为了烂鸟和嘴齿吗？",
			"今天天气真好呀~",
			"为什么会痛苦，一直微笑就好了",
			"没有心，就不会受伤",
			"为什么会变成这样呢……第一次有了喜欢的人。有了能做一辈子朋友的人。两件快乐事情重合在一起。而这两份快乐，又给我带来更多的快乐。得到的，本该是像梦境一般幸福的时间……但是，为什么，会变成这样呢……"
			
	};
	
	public final String list3[] = {
			"嗯","嗯？","嗯...","你以为我啥都听得懂？戳http://operation.tuling123.com/慢慢玩去吧别戳我了啦..."
			,"嗯~","唔..","你就这么喜欢找我吗...","你确定要跟我说话？","你要跟我说话？","你真的要跟我说话？","你真的真的要跟我说话？"
	};
	
	public final String list[] = {
			"神经病",
			"欠骂？",
			"脑子有问题？？",
			"就你还配跟我骂？",
			"nmsl？？",
			"毛病？？",
			"你回去抱着你妈痛哭好吗？",
			"傻逼玩意儿？",
			"毛病？",
			"你个死鸡玩意儿舔你马的塑料批舔上瘾了",
			"我拿烧火棍捅你马大腚眼子",
			"你马原来没批，是爷用射出过你的那玩意儿给开出来的，爷是你马恩人你知道吗",
			"爷直接对着你仙人坟头冲都能让你仙人怀阴胎",
			"你这是看你马批痒了出来替她找人用那玩意儿挠了是吧",
			"你长一岁就离你马进了一步，你马在地底下等你老久了知道吗",
			"爷现在就能让你马对着摄像头露她的大黑批跳极乐净土你信不信？反正我是信的",
			"你再骂一句试试，爷让你知道什么叫烧族谱",
			"秦始皇焚书把你家族谱给焚了你才这么嚣张的吗",
			"爷往你马嘴里滋尿的时候你还只是爷的一颗种子",
			"爷心疼你马不想骂你太多你还蹬鼻子上脸了",
			"你马用来哺育你的不是女乃-汁是她月-经知道吗？你不知道是因为那里面还搀着你兄弟姐妹呢憨批",
			"你这是拿你祖宗牌位擦屁股过了这么狂？",
			"我这辈子头一次有整天想着和生身父亲嘴臭的哈麻批，我用排遗物养你作甚",
			"叫魂呢？！嚎什么嚎！你上次拿针筒当飞机杯卡住姬儿了也没见你这么嚎过",
			"我上辈子是不是拯救了你马才换来和你这nt儿童过家家一样嘴臭的倒霉运？",
			"你嘴里喷出来的都是啥玩意儿？奥力给还是安慕希？",
			"你脑子是在下水道里和你妈臭批一起腌过了这么臭吗？",
			"你可真你母亲的可爱呢小nt还认不认得我了我就是和你母亲对接过的那位啊你忘了？第一次见到你时你可长得真白，就是有点腥，还很粘",
			"曰了猩猩也不至于生出你这么个脑萎缩儿童吧，还敢跟爷爷对线，你先把脑子长到和花生一样大再考虑考虑吧",
			"爷可以考虑一下下次可不可以去曰曰猩猩野猪什么的，给你添几个兄弟姐妹。别伤心啊，到时候你肯定还是家里最丑，最不像人的东西",
			"我化身悟空在你[CQ:emoji,id=128052]批里大闹天宫给你[CQ:emoji,id=128052]一棒子桶的七窍流血 然后把你骨灰杨到你爹的棺材板上 让你[CQ:emoji,id=128052]个你爹永世长眠。我真是个带善人",
			"家里户口本上就你一个吗？孤儿弟弟，小心你爸[CQ:emoji,id=128052]今晚就种枇杷树",
			"你是[CQ:emoji,id=128052]跟你舅舅生的吗",
			"你的治疗留着给你[CQ:emoji,id=128052]治绝症吧",
			"我今晚睡觉前顺便把你的[CQ:emoji,id=128052]狗头铡拉了，杀币",
			"你去转发两条小锦鲤保佑你[CQ:emoji,id=128052]今晚不用死了",
			"要不要老 子给你[CQ:emoji,id=128052]来一套军体拳，两拳把你[CQ:emoji,id=128052]黑p干烂了",
			"火葬场打电话问你[CQ:emoji,id=128052]要几分熟",
			"我永远是对的，你[CQ:emoji,id=128052]永远过世了",
			"你在这样送你[CQ:emoji,id=128052]追悼会我就不去了",
			"你话这么多怎么不在你[CQ:emoji,id=128052]坟头上面慢慢说？",
			"老子把你[CQ:emoji,id=128052]挂黄山迎客松上喜迎八方来宾",
			"我一刀把你[CQ:emoji,id=128052]一九分",
			"我把你[CQ:emoji,id=128052]卵细胞挖出来给你做寿司吃？",
			"已知你嘴臭，你 碧臭，所以你舔过你[CQ:emoji,id=128052]碧",
			"再送让你 九九归一，然后把你[CQ:emoji,id=128052]的头送到兜率宫炼丹",
			"技能都能丢完了？你丢到你[CQ:emoji,id=128052]碧里了？",
			"你非要跟我互动的话看你棺材里面的[CQ:emoji,id=128052]答不答应",
			"逛了这么久，看中哪块坟地适合葬你家里人",
			"今天解密千古悬案之你[CQ:emoji,id=128052]碧里藏尸案",
			"你个b去哪死哪，你[CQ:emoji,id=128052]怎么不死啊",
			"标标标标标尼[CQ:emoji,id=128052]的坟头草五米高"
			,"nmslm"
			,"你妈批着鸡皮又勇猛上岸 不知你那倾情卖逼的野妈猪腿一张淫逼骚水喷涌而出 我只好朝你妈松穴投放原子弹炸去厄立特里亚原始珊瑚礁群上方血肉分离。而你这只疱疹鸡带着你那祖籍越南的废物xx在你奶奶逼里-路滑翔直线从508米下坠穿裂101。"
			,"你妈阴逼外翻外阴阴道念珠菌病群菌性感染让一拳拳交你子宫的你爸爸瞬间手烂硫酸蛆爬满双手淫水顺着你爸爸嘴角留下你爸爸的阴茎癌肉骨肿瘤瞬间好转肉芽肿胀痛进你的宫外孕瞬间激射出你的阴逼死亡下水道。"
			,"一个飞天一击给你婊子妈血槽b上打上一拳，你妈吃痛撅起她的肥臀，被村头野狗看到，狂日八年"
			,"一击洛阳铲劈烂你婊子妈的下体让你这废物还有你民工爹见识见识什么叫曲径通幽处禅房花木深"
			,"不会做人就别做人 去当个牲口闭嘴不好嘛？"
			,"也就你这烂了血逼的废物敢大言不惭的放这种弥天黑屁，再管不好你的鸡嘴老子亲自做一-个剧毒电源精水炸弹塞你逼里-炮把你轰进重症icu与你偏瘫窝囊废爹妈一起安度晚年。"
			,"人生若只如初见，劳资当初就就应在你妈B里插把剑，一刀刺死你个小杂种，可惜你这王八小样儿，壳还挺硬的"
			,"什么弱智新型杂交鱼种麻烦离我远点，你脸部分辨率太高影响老子视觉感受，你妈生你时是不是把人扔了把胎盘养大的，还是普天之下皆你爸，一人一条染色体凑出来组装组成了你?"
			,"今晚必操的你妈横死在努巴尼 你必成为孤儿跪在车前等你妈的死亡通知书召唤你回家"
			,"从小你就暗暗发誓要成为井盖侠，让世界有井无盖，可你父亲拐卖你妈当童养媳的事情暴露被判死刑，无法传承父亲技艺的你受尽凌辱歧视，本该持井盖的双手躁动不安，只能在键盘上一遍遍发泄着自己的无能狂怒"
			,"以后没点本事别出来丢人，你妈卖逼容易吗?"
			,"你一岁死妈，两岁死爸，三岁你姐卖批把你养大，你到现在还不听话"
			,"你个东莞站街逼，自己卖逼得了艾滋浑身都是流脓的骚包还希望谁能接济你?自己穷逼就他妈少往你的烂逼嘴里塞东西，辛苦点多站几次街没准还能攒钱让你妈可以给你哭丧送火葬场下葬。像你这种欠操的臭婊子，倒贴别人的鸡确实不需要别人给你打赏东西好让你塞进你的淋逼，希望你可以和你爸共度春宵或许你爸可以给你5毛钱让你去治病体现父女情深。"
			,"你个石女快去叫雷公给你凿逼吧"
			,"你个窝囊废我操你个死妈你丫全家爆炸，舅舅病房k歌，奶奶灵堂酒会，姑姑丧宴烤尸，侄子送葬摇滚，你妈灵车漂移，你爸坟头蹦迪，给你吃你爹骨灰拌饭，爷爷祖坟歌会，哥哥宗庙拍片，妹妹尸块养猪，媳妇脑浆浇花，全家灵堂派对，姐姐葬礼庆典，弟弟骨髓煮汤，舅妈棺木开花，我踩着你妈棺材板冲浪，给你妈挂树上嘿嘘你妈炸了，炸了你爹一脸你妈的淫水，大骚逼你妈的炸了崩的都不是肉，是他妈老子的精和你妈的淫水。"
			,"你个绿毛龟还是先去医院把龟头疱疹和鸡儿皮包茎治好，你那袖珍鸡巴用显微镜去找不知道能不能找到。"
			,"你从你妈长满蛆的烂臭逼里出来看你完成你妈死前的活替你妈卖逼"
			,"你他妈怕不是从你妈子宫里后空翻转体三周半完美落地脐带勒住脖子大脑损伤变成今天这幅狗b样子"
			,"你以为你就是跨战马提缨枪了么，你可是要被插得头破血流直接进入医院进行生殖器卵保养。不要以为你在脸上涂了点劣质粉底，就可以掩盖你农民的本质。"
			,"你可怜巴巴的来跪在我面前提着你老母亲尸骨未寒的躯体恳求我不要将你这杂种跟你婊子妈一同扔进南海火化"
			,"你在你那个流脓逼上镶嵌一个八星八箭的真水钻，你是看电视购物看傻了么，我简直要用Gucci的假皮带来抽你的烂阴帝，再邀请众多仙家来给你开路下阴，烧纸钱跳大神。"
			,"你在天国和五名乌干达黑人同时拳交致死的亲妈都决定为你起尸鼓掌"
			,"你在我面前玩词汇简直就是班门弄斧 我随随便便打出一个字你婊子妈都能爆体而亡"
			,"你妈一斤多少钱"
			,"你妈东莞卖逼黑直肠烂子宫 生了你这么个梅毒三期红斑狼疮烂全身滥交操坏脑子艾滋没救的便宜女儿，脑子里装了多少阴沟里下三滥的龌龊思想被蛆蛀的天天意淫精神高潮"
			,"你妈今天都要下葬了你怎么还在这里网上冲浪啊？火葬场要下班了哦还不赶紧看看你那骨灰飘零的老母亲？"
			,"你妈住我这放心吧 狠性福爽死她 我他妈先用皮鞭SM你妈 告你爸声敢紧买顶绿帽子戴牢"
			,"你妈在下面混的风生水起 你遗传你妈的滥交性癖给人干的b松的扫把都塞不进去"
			,"你妈在你爹我的的鸡巴上行活塞运动同时疯狂地学着狗吠爸爸我看了很高兴于是一边抽插一边抚摸着你婊子妈yin乱的狗头"
			,"你妈妈逼里的微生物细菌太多了 我把抗原塞你母亲B里我看看能不能清除细菌"
			,"你妈嫩逼又红又痒 估计是我昨天把烧火的铁棍塞进她的黑B里 差点弄得你妈穿肠烂肚"
			,"你妈当初心态失衡的时候我忘劝了，四处放荡，没给你爹少带帽子，还给你传了个入脑的老梅毒"
			,"你妈昨晚挺累的，听说给几十个男人配过了。知道你老子不行，你急着想要弟弟的心情可以理解，但是也不能见男人就替你妈问，你们家当配种玩这么不好，我觉得还是有点感情基础再说"
			,"你妈是我家对面洗脚城的，我操你妈都办了会员卡，我肏你贴白画的亲娘才华横竖都汁溢，淫诗作对简直一流，我出对：“游湖客偶睹我大屌。”你娘亲居然可以回对：“过江人惯肏我续黑屄。”"
			,"你妈死了"
			,"你妈死了也没法挽回对不对 你妈个b嘴臭的时候怎么不想想自己亲妈也得死呢？给你机会给你脸 通通不要 上赶死亲妈 不知道说什么好啊 反正你舍得你妈死 那我就舍得给灵堂办一场体面的葬礼"
			,"你妈死了我是你爹"
			,"你妈死心塌地包养我 她夜夜高潮欲仙欲死 她大姨妈来我都没闲着 她在用观音坐莲这招时一泻千里淫水混合经血淌 "
			,"你妈没拦着你说我是你亲爹吗"
			,"你妈现在在哪个炉子里呢？我去观摩观摩"
			,"你妈生你的时候,把你扔了把胎盘养大"
			,"你妈生你的时候是不是把羊水和脐带挤到你萎缩的小脑里了啊，不然怎么生出你个小猫痴呆的孤儿废物？"
			,"你妈生出了你这个讨shi吃都要被狗咬的孽种然后看到你这逼样果断的把又你塞回你妈的bi里这就是废物的循环利用"
			,"你妈的黑曜石逼无坚不摧，无数嫖客的健壮鸡巴都无法捅爆你妈处女膜只好我亲自出马"
			,"你妈能让我一个屁崩的大小便失禁 我操你妈的 你妈势必今晚横死在家 同时你必变成一个孤儿 臭你妈傻屌 你妈死亡通知书召唤你回家"
			,"你妈自挂东南枝，坐好等鞭尸"
			,"你妈螺旋升天后野爹我恰巧在空中放了个火箭推动器于是你妈接到推动器之后以音速缩水飞向太空这时太阳蒸发了你妈的臭bi水导致你妈无法卖淫"
			,"你妈说养我一辈子 直到我不举的那一天 老子60岁吃两粒伟哥一样能跟你妈在床上大战三百回合"
			,"你妈辛辛苦苦卖逼给各种各样的畜生，被人用了那么多工具就是让你高考440的出来装逼的？"
			,"你妈逼宽又宽，上跑汽车下跑船，日本鬼子来扫荡，一逼夹死七万三"
			,"你妈逼里有黄金我就是黄金矿工把你妈的子宫都勾出来看看是什么得天独厚的臭逼生出了你这个狗东西"
			,"你妈阴道里长了一颗毒瘤 待我一颗手雷扔进去瞬间可以把你妈阴道开拓成宽阔无涯的大黑逼"
			,"你婊子妈在阴间卖逼死也不改老本行。"
			,"你婊子妈子宫肌瘤过多花重金邀请我去给她治疗我就拿着一颗原子弹在她子宫里引爆 "
			,"你婊子妈子宫被原子弹炸的灰飞烟灭后 竟然出现了一片混沌世界 幸运的是我在你妈子宫里开天辟地了"
			,"你婊子妈能让我一个中指大小就干的大小便失禁"
			,"你婊子妈被我开着装甲车碾压成滩烂泥后竟然意外组成了一幅精美无比的迷宫图"
			,"你婊子妈被我用手术刀切开下体在阴唇上缝了一颗定时炸弹 只要你胡作非为你婊子妈必定死无葬身之地"
			,"你婊子妈被我用筷子捅的生不如死，可怜你个孤儿当然泪流满面苦苦哀求让我把你婊子妈送给你日一日。"
			,"你婊子妈那你久无人草的大逼早已披上一层碧绿的青苔。于是我随手撕裂你妈绿色大逼披在m4a1上诞生了m4a1青龙"
			,"你完全是卖逼成了性，不要以为你找根外国吊来日你就可以满足了，还以为你和世界接了轨，你那个逼可是要海纳百川"
			,"你快去你妈的吧 你妈被我倒挂在树上 人仰逼翻 现在正劈着叉大逼朝上呢 我操你损妈 你算个你妈比什么东西"
			,"你是不是害怕你妈恶疽满身的尸体冻在冰里腐烂不了，造福自然的机会都没有？"
			,"你是复读机吗？翻来几句这几个词儿，你可真是胎里带来的弱智成分啊，脑容量有限，只能支持你说这几句了？你在你妈子宫里被人用几把怼傻了吧？"
			,"你是没妈还是没亲妈？从小没人教你素质二字怎么写的？"
			,"你梅毒婊子妈给我开着装甲车念成一滩烂泥竟然组合成了一副精美无比的拼图画拼好后上面写着13个大字:欢迎你陪你妈来阴间卖逼"
			,"你梅毒婊子妈给我开着装甲车碾成一滩烂泥 竟然组合成了一副精美无比的拼图 拼好后上面写着13个大字:欢迎你陪你妈来阴间卖逼"
			,"你死了以后联合国给妈降半旗，因为你免费卖淫给非洲人操，救活一大堆难民"
			,"你母亲张开了血盆大口还真是让我大吃一惊 看来你母亲能狼吞虎咽地吃下我拉的稀屎"
			,"你母亲的肌肤都被用手术刀游刃有余的划边全身 我准备把她的皮做成野生动物的保护膜"
			,"你每个月的收入只够修复你妈了个逼的处女膜 你的鸡巴和逼照样臭得熏天你为了多赚五块钱没带套染了艾滋还回家和你爸一起做"
			,"你爷爷当年用迫击炮对着你妈逼把你轰出来时，可没想到你会变成这样白痴，他一定后悔当年没把你射墙上"
			,"你爸开你妈的棺材在秋名山飙车 给你妈棺材贴个ae86 扫你妈棺材上的福字再送你个敬业福 表扬你妈生前接客365天不休息？"
			,"你爹我一顿暴打揍的你瞎妈的子宫打成稀巴烂浆糊然后把你蠢妈的肠子都拉出来做成美味可口的香肠大发慈悲喂给路边的那些流浪汉吃"
			,"你爹我举世无双的一刀以雷霆万钧不及掩耳之势给你婊子妈全身上下一共切割了七七四十九刀让你婊子妈处于极度痛苦哀嚎中致死"
			,"你爹我往你婊子妈脓逼里安装了photoshop，现在你妈高潮都自动设置高斯模糊，防止你妈兽交的行为被广大人民群众批判"
			,"你爹我拿起一个大喇叭对准你妈逼使用狮吼功把你妈b联动菊花肠子吼下来再和你的大鸡巴互相连接形成了一个人体大喇叭"
			,"你爹我操你妈的时候怎么没把你射墙上呢"
			,"你爹我操你婊子妈臭逼一个闪现到你婊子妈逼里疯狂抽搐让你婊子妈在高潮中升天"
			,"你爹我用核弹轰击你妈子宫并产生巨大能量为城市发电"
			,"你爹我隔着十万八千里给你妈一记回旋踢谁知你亲妈受不了一丁点刺激下体华丽暴毙成为万达广场的华丽喷泉万众瞩目"
			,"你生殖器疱疹还长在你阴蒂外部 蛆壳吃着你逼里的金坷拉 喝着你屁眼里的淫水和你爸爸双飞 在越南跑道泰国人妖场的快感 daybyday飞跃你的性病跨越你的种族激射你的快感。"
			,"你的作为使得你妈无法呼吸明天的空气 你就是你妈的劫 ???"
			,"你的瞎妈已经被我一棍薅到地上趴着不动了 我估摸你的婊子妈现在在畜生道排队等轮回呢"
			,"你的词汇量也是让我不想与你纠缠毕竟你100个爹并没有给你快乐的童年"
			,"你的赞比亚婊子妈昨天跪着求我强奸她被我拒绝，因为我曾亲眼看见你婊子妈在马路上同时被888条野狗抽插她的大烂逼，你妈说她爽到想要起飞，你那怂b短命爹在-旁边看边打飞机憋不住就射在狗屎上，狗屎条塞进你婊子妈的血盆大逼里十月怀胎生下你这个满嘴狗屎的狗头人身杂种。你他妈别恶心人了，我送你台三星note7,你赶紧塞你逼里爆炸送你上天，滚回你的赞比亚老家跟当地野狗一起疯狂繁衍后代"
			,"你的赞比亚婊子妈昨天跪着求我强奸她被我拒绝，因为我曾亲眼看见你婊子妈在马路上同时被888条野狗抽插她的大烂逼，你妈说她爽到想要起飞，你那怂b短命爹在一旁看边打飞机憋不住就射在狗屎上，狗屎条塞进你婊子妈的血盆大逼里十月怀胎生下你这个满嘴狗屎的狗头人身杂种。你他妈别恶心人了，我送你台三星note7,你赶紧塞你逼里爆炸送你上天，滚回你的赞比亚老家跟当地野狗-起疯狂繁衍后代"
			,"你的驴妈就是本皇帝的一个小小玩具，我在她阴道里种植的西红柿就是你拍手称赞的最佳晚餐"
			,"你脑子里是有通古斯爆炸留下的坑？"
			,"你脖子上是个肿瘤么？"
			,"你话那么多怎么不坐你妈坟头上跟她慢慢说。"
			,"你说你妈呢, sb nmsl"
			,"你说你妈的崽种, 你妈死了吧"
			,"你赶紧把肠子收拾的紧紧的，别怠慢了恩客们，指望另寻金主！等下洗干净乖乖躺床上捆好自己别动，让驴屌大哥们搅的叫你高潮迭起不得升天！闭上你的狗嘴，放你家那臭私窠子淫妇歪拉骨接万人的大开门黑猩猩臭屁，至于客人等下肏不肏屁眼，管你鸡巴事，反正横竖没肏你爹罢了"
			,"你这三等残废操作笑死老子了。你们家还按照品种认爹？"
			,"你这个RBQ别说话"
			,"你这个烂货，完全是当今社会的毒瘤，老子要在你烂MP里面安一个定时炸弹，3次密码不对，马上爆炸。"
			,"你这个狗杂种还真是死皮赖脸 我在你亲妈嘴里拉了稀屎 看她气喘盱盱的样子就知道她吃的非常痛快"
			,"你这土狗只能看着老子无情说出你家的悲惨身世不敢讲话，只能在家默默吃shi"
			,"你这小哈巴狗面目狰狞的看着手机屏幕不停地复制黏贴你要是不抽筋了你婊子妈都能被你活活气死"
			,"你这废物鸡巴跟牙签一样，插到你母狗妈的水缸逼里都搅不动，天天在这对太阳乱吠也不会有三眼怪过来捡你回家喂狗粮的。"
			,"你这样寻爹无异于大海捞针啊，别误会，我说的大海捞针不是说你妈松你爹细，别误会啊，就是个成语，别见谁就想碰瓷喊爹，我又不会汪汪叫"
			,"你这种烂货，完全是当今社会的毒瘤，老子要在你狗脑子上面安一个定时炸弹，3次密码不对，马上爆炸，正好为社会除害。"
			,"你这种站街讨饭骚逼 只适合拿根钢管跑到大街上旋转跳跃 逼饿了就捅一捅流下来的逼水 填满了整个东非大裂谷 引来乌干达阳痿男的牙签鸡巴塞满你脸上那逼嘴 你大叫着爽爽爽我吃的好饱再也不用蹭别人吃的啦 我吃鸡巴就够啦谁料阳痿男常年不射精输尿管里长了结石你帮他打嘴炮吸出来一大坨结石滑入你的支气管里你当场毙命"
			,"你这词穷土狗复习了大半年你的小学造句对爹也是毫无伤害殊不知你婊子妈逼水四射的莲花烂逼早已被爹肆意践踏变得干枯无力"
			,"你那臭婊子妈被大猩猩折磨致死你野爹我用杀猪刀把你婊子妈的四肢砍断装在袋子里做成便携肉便器解决你野爹我的生理需求"
			,"你野爹我提起30斤大鸡巴在你裱子妈翻毛大血比疯狂抽插也是让野爹我感受到了无双的快感"
			,"你骂我也没办法改变你妈暴毙而亡逼里生蛆的事实"
			,"你鼻子下面是牛欢喜啊，请你不要用你的欢愉器官对我说话，这是很不礼貌的，谢谢你的合作。我能理解你的难受，毕竟万里长城永不倒，你的烂嘴要人搞"
			,"俗语说淫贱者乐山，弱智者乐水，这句话的意思是你最喜欢在你淫贱公妈逶迤的甬道中徜徉翻滚，腾出阵阵细浪，好像滔滔江水连绵不绝，又有如黄河泛滥再来一发不可收拾。"
			,"傻儿子快来你婊子妈在爹胯下疯狂输出说要见你最后一面要跟你爹我决战到天亮"
			,"傻逼东西"
			,"傻逼，1分钟你憋不出三句话，这个时间够给你妈刮宫四次了，你妈手脚再快一点的话，也有七八个人射精了。"
			,"八国联军在日你的婊子妈的时候没给你做好胎教吗"
			,"再倒贴你你野爹烂鸡巴在你妈子宫骨瘤朵朵爆裂炸穿子宫壁"
			,"别人开店叫便利店，你开店叫妈逼专卖店。"
			,"别看你婊子妈高贵冷艳 只要我媚眼一抛 你婊子妈必定得给我大牛子服侍个二天二夜"
			,"只见我扣动扳机立刻发射无数子弹扫射你妈大逼后还发出阵阵龙啸声。你妈骄傲的说自己大逼就是龙吟山庄"
			,"可怜兮兮的杂种狗送来亲妈不知珍惜被我一招暴雨梨花针击穿大脑鲜红地血液洒满大地;"
			,"吃这么多经济是用来买纸钱祭奠你暴毙的娘？"
			,"和你这种臭傻卵互动真的恶心人 就你还瞧不起猪饲料 我寻思你不是从小吃到大的 你也配？"
			,"哇，Fucking crazy"
			,"哥布林之王在你妈逼里打开了一道异次元裂缝，魔兽军团籍此冲进来准备和你妈逼里的无数蛆虫展开一场惊天地泣鬼神的绝世大战"
			,"在天国和五名乌干达黑人同时拳交致死的亲妈都决定为你起尸鼓掌。"
			,"在这狂吠是能给你死去的全家弹奏一曲东风破？"
			,"在？你妈遗照？发来一张？"
			,"城外三十里火光冲天烧的就是你妈飘零的骨灰。"
			,"孟德尔是大科学家 尤其是发明了杂交育种 原来当初发明杂交育种的时候就是用你妈做的实验 怪不得能这么成功 你这杂种被爸爸打的气都喘不过来了 你是不是准备用高锰酸钾制取氧气 来维持自己苟延残喘的生命"
			,"实不相瞒 其实我是搞房地产的 我的朋友圈就是一座坟场 有人的不想活了来我这找死 还得带着爹带着妈 现出售墓地哈 谁妈死了 我帮你埋 我操你妈 ?"
			,"实在气不过希望你速速跪下感谢你爹操你这狗婊子"
			,"对于粗口的运用，我已经达到了非常娴熟的程度，一句“草你婊子妈死了”主要采用现实主义和理想主义结合的方法，简洁、明了而又清晰，既能抒发当下你婊子妈人人遭恨，人人想轮的情绪，又具有很深的心理内涵。“草你婊子妈死了”所蕴含的启蒙主义思想特征，主要表现在对阁下是畜生狗杂种出身的深刻揭露，对阁下弱智文盲连粗口都不会几句的批判，以及广大人民群众热切和令堂深入交流的欲望，同你妈悲惨命运的归属。"
			,"对面在亮狗牌，上面写满你妈死了。虽然说你不知道你爹到底是谁，但是架不住你能在排位里乱认啊"
			,"小bk的瞧你那揍性一身囊揣，喊你臭狗屎都抬举你"
			,"就这？就这？"
			,"崽种给爷死"
			,"废物来玩举报可以我坐在你妈坟头上等着。"
			,"建议你火葬场直走哦你这个废物怎么还在这撒泼呢？是不是绝症晚期没钱治病呢？还是你那婊子妈卖b的钱还不够你看看你的晚期脑瘫？"
			,"建议掏出你妈子宫磨亮了照照自己 你配和我谈条件吗？"
			,"当时跟你爸生你的时候把你射墙上扣都扣不下来"
			,"当然爹知道你这废物在电脑旁瑟瑟发抖说爹复制粘贴然而废物东西跟你智障爹一样怂的一B要不爹就不会给你智障亲爹带那么多次帽子"
			,"心疼你这个废物连初中都没读完枉费你婊子妈一大把年纪接客给你读书的钱你却跑去上网来喷你爹我"
			,"您这样要是叫好好说话的话，那么社会主义伟大复兴早就实现了，建议你滚回令堂子宫里去回炉再造智商，实在不行是家族智商不够的，等着你爹妈生二胎的时候，买本《科学备孕》送你爹妈，再去养猪场排队交钱配种，给你弟弟加个智商Buff，让你可以用你弟弟脐带血整整脑子？"
			,"想把你这个土锤赛回你妈的子宫好好重造一翻 哪知道你妈的子宫被你野爹-拳打碎 导致你和你妈一尸两命 你这个废物到现在还神智不清"
			,"我一拳将你婊子妈的头打开花，然后再将你婊子妈碎尸万段，丢下你个孤儿开始胡言乱语前来送妈。"
			,"我一根中指就能把你妈干到脑抽筋"
			,"我一棍子给你打回你妈逼里 欢迎你在你妈逼里看你妈在阴间卖逼 看着长枪在你妈逼里进进出出 捅的你妈肠穿肚烂"
			,"我一铁锹给你头盖骨掀开 好好看看你脑子被蛆吃的还剩多少 你妈天天高者挂罥长林梢下者飘转沉塘坳 尘归尘土归土 孤儿不配有老母"
			,"我不草你妈你就不知道我是你爸啊"
			,"我也是一脚踢碎你婊子妈的子宫生怕你婊子妈在生出来和你一样的弱智"
			,"我今晚干你妈不用鸡巴只用扫把"
			,"我他妈踹烂你妈逼你妈还让野狗舔，你说你妈骚不骚，你说你妈逼有多臭"
			,"我即将为你母亲做移植手术 稍后你会看到母猪的子宫接二连三的进入你母亲子宫囤积能量"
			,"我去你妈了个蝴蝶螺旋大骚逼，你妈追我三条街说我长得像你爹"
			,"我双手拳交你亲妈 把你臭妈多操出一个尿道 操你妈的 滚"
			,"我叼你妈的"
			,"我在你妈逼里自由翱翔你妈哭着夸我真强"
			,"我宰杀你梅毒婊子妈 你妈被我五马分尸 头被我拧下来挂在城楼上 路过的人都要给你妈倒杯酒祝她地狱卖逼继续红火"
			,"我家小鲤鱼简直想在你妈臭嗨里历险。"
			,"我左右扔出一颗毁灭火莲 顿时间你婊子妈的阴道深处冒出一团熊熊火焰 真是壮观无比"
			,"我当年已经把你射到墙上了，可你妈又把你刮下来塞进B里要死要活求求我房放过她肚里的孽畜"
			,"我心疼你个废物连初中都没读完 枉费你婊子妈一大把年纪接客 给你读书的钱你却跑去上网来喷人"
			,"我想插你了，在春风十里，在万株桃花，在青幽竹林，在拱桥月下。"
			,"我想插你了，在罗马的斗兽场，在迪拜的帆船酒店，在奔腾的壶口瀑布，在平静的塞纳河上，在神秘的英国城堡，在秋风凌厉的长城，在故宫的城楼，在秋日火红的枫林。 ???"
			,"我手持方天画戟在你妈逼里肆无忌惮的挥舞 最后给你妈子宫内壁留下了不可磨灭的痕迹"
			,"我把你妈剁成250块扔进会唱歌的火炉里边烧边唱自由飞翔"
			,"我把你婊子妈的阴唇外侧涂满了沙拉酱放到太阳下竟然能进行光合作用真是奇妙至极"
			,"我把你爹几把剁下来给你妈烧香"
			,"我拿着阴阳斩龙斧把你娘的狗头撕裂后无形坠入到了万丈深渊里被深渊巨鲨撕咬的粉身碎骨最后被土崩瓦解了"
			,"我操你妈你也是惊天大婊子了还他妈想给老子泼脏水有这功夫把你自己被野狗咬到路上的肠子收拾收拾塞回自己那张烂嘴里吧再给老子泼脏水你直系亲属死一个手tm这么长逼脸那么大塞你妈子宫糜烂臭气熏天大黑洞里有时间给你妈治性病再倒贴我一下把你爹从坟地里刨出来送去你上班床上跟你打炮看塞得塞不住你的贱嘴"
			,"我操你妈的时候你爸还在边上看呢，边看边说老子草的好，夸老子让你妈爽了，你他妈知道为什么吗?因为你爹他妈的阳痿鸡巴让我切了!"
			,"我操你妈的逼，一脚给你妈逼踹开花了，跟他妈你现在脸盘子一边大"
			,"我操翻了你这个杂碎的亲妈 的时候 你爹吓得躲在床头柜里不敢出来 我发完这句话你妈也在我胯下咽下了最后一口气"
			,"我日你妈大血逼螺旋升天，我还把你妈肠子抽出来塞进她嘴里，你也只能这个表情看着了，废物"
			,"我是你爹"
			,"我是你的爸爸"
			,"我是你野爹倒是不假你野爹我把你妈逼塞进八卦炉里经过七七四十九天你妈逼就金刚不坏了"
			,"我是弟弟？我还真是弟弟，毕竟咱祖上就有玩嫂子的优良传统，因为你永远硬不了，永远都硬不了。"
			,"我是操你妈的行家 不管风雨的操你妈 一边操一边笑今天的高潮淫水喷得好"
			,"我点燃一枚子母雷扔进你吗下体你妈瞬间化为废墟，你还一边舔着你妈血肉模糊的鱼雷比一边喃喃道：入口即化，纵想丝滑"
			,"我用你妈的梅毒子宫为你弹奏一曲东风破。"
			,"我用剃刀把你妈子宫取出来煲一顿鲜嫩可口的汤喝"
			,"我看你妈得了淋菌性尿道炎 也要强忍鸡巴戳穿流脓逼的快感 得到你爸爸的脓精子 然后冒着艾滋母胎传染危险 生下你这个生来软下疳的畜生 从村头打铁t逼老王到村尾拉皮条卖鸡花老刘你都睡到一层两层三四五层绿帽子给你鸡巴肉肿瘤的爸爸。"
			,"我看你婊子妈子宫内壁缺了一块 我拿一块浆糊用透明胶给她微微一粘贴 你妈子宫瞬间变得完好无损"
			,"我知道你一下子接受不了这么大的信息量，但是教了你这么多次出牌先出炸、射人先射妈，还是学不会，充分暴露了你是一个弱智的本质。毕竟你的大脑只有单线程，但是日子久了你会发现你妈还是你妈，就是你爸可能每天一张新面孔了。不过你这样的低能拖油瓶，不知道哪个倒霉鬼会接盘"
			,"我空降你爹葬礼用你妈子宫弹奏一曲dj版求佛为你爹亡灵超度。"
			,"我空降你爹葬礼用你妈子宫弹奏一首dj版求佛为你爹的亡灵超度"
			,"我素质低？我是没有素质 操你妈都是轻的 我操你的血妈"
			,"我要倒立和你妈来一炮才能解气"
			,"我那爱斯基摩烈性犬爱上了你妈的血逼并那它们那碗口粗的JB向里捅去殊不知你那婊子妈爽得屁滚niao流你这废物当时开怀大笑"
			,"我骑着你爸上你妈，难度系数9.8"
			,"我高举大刀正劈到你的头颅然后迸发出的血液流到地上构成了一幅泼墨山水画，你婊子妈看到了直接高潮"
			,"打团的时候跑这么快赶着去参加你妈葬礼？"
			,"扔块肉在手机上，狗都比你会说话。你狗爹在火葬场都凉了大半天了你这个憨批儿子还到处乱叫。你妈被我五马分尸，头被我拧下来挂在城楼上 路过的人都要给你妈倒杯酒祝她地狱卖逼继续红火"
			,"把你爷爷榨汁灌你爹马眼射你p眼三代团圆"
			,"提升智商还有个简单的方法，上次我建议你妈和野猪配种生杂种用脐带血进行移植手术，如果你家实在没钱做手术的话，可以采用打开脑子——把脑子倒进马桶——把屎装进脑子——缝好脑门，这个简单又别致的方法，注意事项是别低头，不然屎水会从鼻子和嘴巴里漏出来，嘴边挂着棕色哈喇子见人素质低下"
			,"操你妈千遍也不厌倦 操你妈的感觉像初夜 我当年开你妈苞骑 操你姥 迷奸你奶 颜射你妹 我会乱说吗"
			,"操你妈的穷酸废物你野爹一拳轰炸你狗娘的子宫防御系统让你宛如丧家之犬的哀嚎"
			,"故事发生在前天晚上的狗笼子里，你妈300斤体重上网挂牌卖逼出价一千五一晚，由于使用伪劣产品参与市场竞争哄抬逼价，已经遭到卫生署出警进行紧急人道毁灭。消息出来后得到众多上当受骗的消费者一致叫好，大家纷纷感谢国家让他们告别选妞如送命，上钟如上坟的痛苦日子，你马上风崩殂的野爹泉下有知也深感欣慰。"
			,"本爷爷一刀砍下你爸那弱不经风的生殖器官直插你婊子妈血逼里万万没想到发生裂变反应而导致处女血和精液一起喷薄而出"
			,"本野爹我用虎式坦克击落然后后空翻落地之后子宫炸裂一堆蛆虫看你妈的血逼很银荡便在你妈的血逼里筑了个巢"
			,"杂种东西玩不起别瞎bb亲娘性命难保还要瞎鸡巴装逼"
			,"来群里奔丧来了，家里哪个亲人去西方极乐净土了？"
			,"枯藤老树昏鸦，小桥艹了你妈，夕阳西下，你妈死在天涯"
			,"死母狗发你吗逼的春啊骚东西一个也有逼脸在这瞎几把吠被你家楼下的老黄狗舔高潮了是吗？这是你犯贱的理由吗？"
			,"殊不知你爹我一个不小心用力过猛一巴掌将你婊子妈的狗头拍出脑震荡成了真正的白癡"
			,"江南北都有你婊子妈的嫖客你妈妈的卖屁股生意边际全世界而你妈妈的下体已经被联合国认定为最好的肉体"
			,"没妈的孤儿闭嘴吧"
			,"滚你妈逼里撅着去 老子用大鸡八把你杵出来看你妈卷毛黑蒂大血逼 下一个被骑操三年的倒霉鬼就是你"
			,"火葬场的老王告诉我，你妈糊在锅底扣不下来。你爸去救她的时候被灵车漂移撞死了。两人情侣套餐9.9打包了，骨灰飘洒人间，你个废物小孤儿还不赶紧去火葬场看看你的爹娘？"
			,"煞笔东西别他妈烦老子"
			,"煞笔公狗让你几把手欠了吗跟你爹装啥比啊瞧把你能的你是不是你吗的白带吃多了啊你告诉我死狗说话啊煞笔老子把豌豆射手塞你妈逼里"
			,"爹真的心疼你年迈的婊子妈当年被爹疯狂中出怀胎十月有了你然而废物一点都不随大爹我心疼"
			,"爹顺手一杆子刺穿你婊子妈臭逼跟你狗爹尸体穿在一起以110迈速度将你婊子妈智障爹扔去月球以示地球人热爱和平"
			,"看你婊子妈在化粪池里游泳烂的跟你那鸡巴一样的脸差不多还在叫着自己怎么的烂逼是怎么被野狗咬烂的"
			,"窝囊废要不要点逼脸你个骚几把东西，你贱成这样吓得你妈一晚上被五百只公猪轮奸"
			,"给你妈签一个一会就到期的生死薄。"
			,"给你栓条链子你就能安静的多你这废物来送妈也不看看自己的民工爹挥汗如雨的惨状还是得我用意念一巴掌扇进你妈的狗头扭转乾坤"
			,"给老子滚"
			,"老子今天可是充满了战斗力，替天行道撕下你的假阴帝。"
			,"老子进小黑屋跟你妈做爱跟上厕所-样随意 你妈不吃延更丹都闭经啦 就当你爷俩面抽插你妈大黑逼"
			,"臭不要脸倒贴上赶着脱裤子撅屁股的贱样儿 也不看人家拿不拿正眼儿瞧你，嗑的药是250手的水货大麻吧直接拉野地里喂狗"
			,"臭表子瞧你那黑逼臭成什么几把样子了也就你家楼下的老黄狗愿意舔你那留绿汁的逼啊"
			,"请让我先捅烂你亲妈臭逼为敬"
			,"负责拍摄你妈AV的小老鼠正在你妈的B里跳拉丁舞想不到一向谨慎的小老鼠看了你母亲的AV兽性大发给你狗娘的下体踩的轰隆作响"
			,"这个孤儿每次被我辱骂都暂时性获得亲妈，正可谓无妈胜有妈，无妈的孩子像块宝 ???"
			,"这年头卖肾挺值钱的 我趁着你母亲呼呼大睡 用瑞士军刀割开她的腹部取走双肾卖了一百万零花钱"
			,"这边建议您早点没哦！要不然墓地涨价了可不行~"
			,"造谣一时爽 全家火葬场 再不滚出来道歉今晚你妈灵堂开party"
			,"野爹我一剪刀戳入你狗妈屁眼子,疼的你婊子妈下体yin水泛滥,你这逆子却把你妈的逼改造成了水力发电站,你可真是造福人类呢"
			,"野爹我把你妈白带抽出对着你个杂种狗操的逼脸狂抽一阵你就把一整天吃的shi吐了出来"
			,"阁下中午吃奥利给噎着了是怎么着，用为父来点稀的给你溜溜缝儿吗"
			,"骂我？你妈是在我胯下断气的。我天外飞仙空降你妈的葬礼 用你妈的梅毒子宫弹奏一曲东风破 你妈遗像笑得美 我爱你妈"
			,"骑在你的狗头上望着你婊子妈你婊子妈与我在你的狗脑袋上共度良宵可是你这废物只敢缩着脑袋说：我也想日啊"
			,"黄金狗什么垃圾操作也敢秀优越，麻烦门口树上取妈，带着她一起看你爹划过天际点亮星空片片飘散的样子"
			,"我sha你[CQ:emoji,id=128052]"
	};
	
	class TestHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
        	System.out.println("收到http请求，来自:" + exchange.getRemoteAddress().getHostString());
        	k.sendPrivateMSG(owner,"收到http请求，来自:" + exchange.getRemoteAddress().getHostString());
        	exchange.sendResponseHeaders(403, 0);
        	OutputStream os = exchange.getResponseBody();
        	OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
        	writer.write(new String("拒绝访问。"));
        	writer.close();
        }
    }
	
	public QQrobot(KQWebClient k) {
		this.k = k;
		init();
	}
	
	public QQrobot(URI uri) {
		this.k = new KQWebClient(uri);
		init();
	}

	public void init() {
		System.out.println(new Date().toLocaleString());
		k.addQQMSGListenner(this);
		managers.add(owner);
		managers.add("xxx");//内定管理
		root = Preferences.userRoot().node("QQrobot");
		try {
			root.removeNode();//清空数据,完全依赖data.xml
			root = Preferences.userRoot().node("QQrobot");
			read();
			HttpServer server = HttpServer.create(new InetSocketAddress(5408), 0);
	        server.createContext("/", new TestHandler());
	        server.start();
	        
	        sheet = Workbook.getWorkbook(new File("mark.xls")).getSheet(0);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread(()->{
			try {
				while(true) {
					Thread.sleep(2000);
					if(new Date().getHours()==0&&new Date().getMinutes()==0)
						Thread.sleep(3000);//确保所有线程都报时
						timesOfReread.clear();
						contentOfReread.clear();
						memberOfReread.clear();
						contentOfLastReread.clear();
						hasLooked.clear();
						Thread.sleep(70000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		
	}
	
	/**
	 * 发送 请求成员数据 后 处理返回结果
	 */
	@Override
	public void checkPermission(int permission,String QQID) {
		this.permission = permission;
		
		if(QQID.equals(myself)) {
			isManager = permission==1?false:true;
		}
		
	}
	
	@Override
	public void RE_ASK_ADDFRIEND(AddFriends arg0) {
		k.sendPrivateMSG(arg0.getFromQQ(), "您配加我吗？");
	}

	@Override
	public void RE_ASK_ADDGROUP(ADDGroupExample arg0) {
		whyComeIn = arg0.getMsg().split("答案：")[1];
		k.agreeAskGroup("1", "1", "I like.", arg0.getResponseFlag());
	}

	@Override
	public void RE_EXAMPLE_AMEMBER(AddAdmin arg0) {
		String qq = arg0.getBeingOperateQQ();
		String g =arg0.getFromGroup();
		if(qq.equals(myself)) {
			return;
		}
		
		k.sendGroupMSG(qq, g, (optgroup.contains(g)&&isAlive)?"新来的找骂？":((arg0.getSubType().equals("2")?
		"Ohhhhh,[CQ:at,qq="+arg0.getFromQQ()+"]把[CQ:at,qq="+qq+"]拉进群了呢,":
			((whyComeIn != null && !whyComeIn.equals(""))?"通过"+whyComeIn+"入群的呢":"管理员[CQ:at,qq="+arg0.getFromQQ()+"]同意了新人入群。"))
				+"\\n[CQ:at,qq="+qq+"]新来的您好呀~"), false);
		if(!isAlive&&r.nextBoolean()) {
			k.sendGroupMSG(qq, g, "来人啊，迎新啦！", false);
			k.sendGroupMSG(qq, g, "宁来了啊~", true);
			k.sendGroupMSG(qq, g, "大爷", false);
			k.sendGroupMSG(qq, g, "玩玩儿？", false);
			k.sendGroupMSG(qq, g, "嗯♂", false);
			k.sendGroupMSG(qq, g, "宁要御姐还是萝莉", false);
		}else {
			k.sendGroupMSG(qq, g, "[CQ:image,file=xl"+(r.nextInt(2)+1)+".jpg]", false);
		}
		
		whyComeIn = null;
	}

	@Override
	public void RE_EXAMPLE_DEMBER(DeleteAdmin arg0) {
		String g = arg0.getFromGroup();
		k.sendGroupMSG(arg0.getBeingOperateQQ(), g, (optgroup.contains(g)&&isAlive)?"刚刚有人因为骂不过我而永远离开了我们。":"呜呜呜，有人默默离开了我们...", false);
	}

	@Override
	public void RE_EXAMPLE_MANAGE(RE_MSG_AdminChange arg0) {
		String qq = arg0.getBeingOperateQQ();
		String g = arg0.getFromGroup();
		if(arg0.getSubType().equals("1")) {
			k.sendGroupMSG("", g, 
				(qq.equals(myself))?"啊群主大哥啊我错了我错了啦别撤销我管理啦呜呜呜...":("哦不！刚刚[CQ:at,qq="+qq+"]因为搞事情被撤销了管理员资格！"), false);
		}else if(arg0.getSubType().equals("2")){
			k.sendGroupMSG("", g, 
					(qq.equals(myself))?"终于混上管理啦！":"恭喜[CQ:at,qq="+qq+"]刚刚成为了管理员！", false);
		}
	}
	
	/**
	 * 本方法用于将\r\n转换为\\r\\n(lemoc接口需要....)
	 *解决String自带的replace无法替换逃逸字符的问题
	 * @param s 要去除换行符的字符串
	 * @return 处理后的字符串
	 */
	public String replaceNewLine(String s) {
		
		if(!s.contains("\r\n")) {
			return s;
		}
		
		StringBuffer sb = new StringBuffer(s.replaceAll("\r\n", "\\r\\n"));
		for(int i = 0;i<sb.length()/*细节1*/;i++) {
			if((sb.charAt(i)=='r'&&i!=sb.length()-1/*防止下标越界*/&&sb.charAt(i+1)=='n')
					||(sb.charAt(i)=='n'&&sb.charAt(i-1)=='r')) {
				sb.insert(i, '\\');
				i++;//细节2
			}
		}
		
		return s=sb.toString();
	}
	
	/**
	 * 一不小心就写了这个东西..
	 * @param group 参与复读的群
	 * @param qq 查询者qq（若为定时播报则传入null）
	 * @param showDetail 是否输出细节 （细节包括：复读时间，每次复读人数以及分别是谁）
	 * @return 复读内容列表
	 */
	public String getRereadList(String group,String qq,boolean showDetail) {
		ArrayList<Date> dates = timesOfReread.getOrDefault(group, null);
		
		int times = 0;
		if(dates==null) {
			return "本群复读总数：0\\n太棒了！这是个好习惯！请继续保持噢！\\n";
		}
		String ans = "群复读总数："+dates.size()+"\\n";
		String ans2 = ans;
		
		ans += "分别在：\\n";
		boolean isImage = false;
		for(Date d:dates) {
			
			HashSet<String> hs = memberOfReread.getOrDefault(d, new HashSet<String>());//防止无人参与复读

			ans += "\\n" + d.getHours()+"时"+d.getMinutes()+"分"+d.getSeconds()+"秒" +"\\n内容是：";
			String s = contentOfReread.get(d);
			if(s.contains("[CQ:image")) {
				ans += "“[图片消息]”";
				isImage = true;
			}else {
				ans += "“"+replaceNewLine(s)+"”";//防止内容出现换行
				isImage = false;
			}
		
		
			if(qq!=null&&hs.contains(qq)) {
				ans+="*\\n";
				times++;
			}else {
				ans += "\\n";
			}
			
			ans+="参与人数："+(hs.size()+(isImage?0:1))+"\\n分别是：";
			ans+= isImage?"\\n":"我自己";
			for(String q:hs) {
				ans+= ",[CQ:at,qq="+q+"]";
			}
			ans+="\\n";
	
		}
		
		ans+="\\n";
		if(qq!=null) {
			if(times != 0) {
				ans+="您共参与了"+times+"次，带“*”号表示您参与的复读。\\n珍爱生命，远离复读。珍惜时间，从我做起。\\n";
				ans2+="您共参与了"+times+"次，\\n珍爱生命，远离复读。珍惜时间，从我做起。\\n";
			}else {
				ans+="您一次也没参与，真是太棒了呢！";
				ans2+="您一次也没参与，真是太棒了呢！";
			}
		}else {
			ans += "\\n珍爱生命，远离复读。珍惜时间，从我做起。\\n";
		}
		
		return showDetail?ans:ans2;
	}
	
	public void addDate(String msg,String group) {
		ArrayList<Date> dates = timesOfReread.getOrDefault(group, new ArrayList<Date>());
		Date d = new Date();
		contentOfReread.put(d, msg);
		dates.add(d);//记录时间
		timesOfReread.put(group, dates);
	}
	
	@Override
	public void RE_MSG_Group(RE_MSG_Group msg) {
		
		String m = msg.getMsg();
		String qq = msg.getFromQQ();
		String g = msg.getFromGroup();

		if(cntOfAngry<500) {
			cntOfAngry++;
			if(m.contains("[CQ:at,qq="+myself)) {
				k.sendGroupMSG(qq, g, "[CQ:image,file=sq.jpg]", false);
			}
			return;
		}
		
		if(m.contains("[视频]你的QQ暂不支持查看视频短片")) {
			return;//不处理视频消息（好吧是没办法）
		}
		
		if(contentOfLastReread.getOrDefault(g, "").equals(m)) {//复读检测
			if(r.nextInt(10)==1/*概率打断*/) {
				k.sendGroupMSG(qq, g, list6[r.nextInt(list6.length)], false);
				return;
			}
			if(!hasReread.getOrDefault(g, false)) {
				k.sendGroupMSG(qq, g, replaceNewLine(m), false);
			}
			ArrayList<Date> dates = timesOfReread.get(g);
			Date d = dates.get(dates.size()-1);//获取最新时间为key储存
			HashSet<String> t2 = memberOfReread.getOrDefault(d, new HashSet<String>());
			t2.add(qq);//记录某时间点参与复读的用户
			memberOfReread.put(d, t2);
			
			return;//防止问句答句一致
			
		}else {
			contentOfLastReread.put(g, m);
			hasReread.put(g, false);
		}

		if(msg.contains("[CQ,")&&(msg.contains("99")&&msg.contains("66"))){
			return;
		}
		
		if(!timeMap.getOrDefault(g, false)) {
			
			timeMap.put(g, true);
			
			new Thread(()->{
				try {
					while(true) {
						Thread.sleep((r.nextInt(10)+10)*60*1000+(r.nextInt(10)+2)*60*60*1000);//每隔一段时间说句随机废话
						if(!hasReread.getOrDefault(g, false)) {
							if(r.nextBoolean()) {
								k.sendGroupMSG(qq, g, sexygroup.contains(g)?list4[r.nextInt(list4.length)]
										:list2[r.nextInt(list2.length)], false);
							}else {
								k.sendGroupMSG(qq, g, "[CQ:image,file=kq"+(r.nextInt(2)+1)+".jpg]", false);
							}
						}
					}
				}catch (Exception e) {
				}
			}).start();
			
			new Thread(()->{//定时播报
				try {
					while(true) {
						Thread.sleep(2000);//有点浪费的样子
						if(new Date().getHours()==7&&new Date().getMinutes()==0) {
							k.sendGroupMSG(qq, g, "清晨起来！拥抱太阳！满满的正能量！今天，准备好复读了吗？", false);
							k.sendPrivateMSG(owner, "七点报时");
							System.out.println("doit");
							Thread.sleep(70000);
						}
						if(new Date().getHours()==12&&new Date().getMinutes()==0) {
							k.sendGroupMSG(qq, g, "嘟...十二点了呢...(我就是刷个存在感怎么了嘛o(TヘTo))", false);
							k.sendPrivateMSG(owner, "12点报时");
							System.out.println("doit");
							Thread.sleep(70000);
						}
						int times = timesOfReread.get(g).size();
						if(new Date().getHours()==0&&new Date().getMinutes()==0&&times!=0) {
							k.sendGroupMSG(qq, g, "嘟...昨日"+getRereadList(g,null,true), false);
							k.sendPrivateMSG(owner, "0点报时");
							System.out.println("doit");
							Thread.sleep(70000);
						}
					}
				}catch(Exception e) {
					
				}
			}).start();
		}
		
		if(managers.contains(qq)) {
			if(m.equals("黑化")) {//基本从良了
				optgroup.add(g);
				k.sendGroupMSG("", g, isAlive?"准备开骂":"唔..我已经准备好无脑喷了，可是总开关还没开呢...", false);
			}else if(m.equals("娘化")){
				optgroup.remove(g);
				k.sendGroupMSG("", g, "亲，嗯，有什么需要服务的吗？", false);
			}else if(m.equals("发骚")) {
				sexygroup.add(g);
				k.sendGroupMSG(qq, g,list4[r.nextInt(list4.length)],false);
			}else if(m.equals("停止发骚")) {
				sexygroup.remove(g);
				k.sendGroupMSG(qq, g, "天，我刚刚怎么了？？", false);
			}
		}
		
		if(!managers.contains(qq)&&isAlive&&optgroup.contains(g)) {
			if(m.contains("[CQ:at,qq="+myself+"]")) {
				k.sendGroupMSG(qq, g, "艾特你妈呢？？还配艾特我？", true);
			}else if(m.contains("机器人")){
				k.sendGroupMSG(qq, g, "机器你妈？傻逼玩意儿", reAt);
			}else{
				k.sendGroupMSG(qq, g, list[r.nextInt(list.length)], reAt);
			}
		}else
			
		if(m.contains("成绩查询")) {
			try {
				String ms[] = m.split(" ",2);
				
				if(!ms[0].equals("成绩查询")||ms[1].equals("")) {
					throw new Exception();//懒
				}
				
				if(ms[1].equals("xxx")) {
					k.sendGroupMSG(qq, g, "甭查了，肯定第一。", false);
					return;
				}
				k.sendGroupMSG(qq, g, "最新一次考试成绩（xxx）查询中，请稍等...", false);
				
				String res = "@#$";
				for(int i=0;i<820;i++) {
					if(sheet.getCell(1, i).getContents().contains(ms[1])) {
						res = "";
						for(int j = 0;j<sheet.getColumns();j++) {
							res+=sheet.getCell(j, 1).getContents()+":"+sheet.getCell(j, i).getContents()+"\\n";
						}
						k.sendGroupMSG(qq, g, res, false);
					}
				}
				if(res.equals("@#$")) {
					k.sendGroupMSG(qq, g, " 啊偶，找不到这个人呢...", true);
				}
				
			}catch(Exception e) {
				k.sendGroupMSG(qq, g, " 用法:\\n成绩查询+<空格>+姓名（可根据姓名中的字配对）", true);
			}
		}else
			
		if(m.contains("清除节点")&&managers.contains(qq)) {//放在最前面判断防止被调教命令语句时无法清除节点
			try {

				String ms[] = m.split(" ",2);
				
				if(!ms[0].equals("清除节点")||ms[1].equals("")) {
					throw new Exception();//懒
				}
				
				if(root.nodeExists(ms[1])) {
					root.node(ms[1]).removeNode();
					question.remove(ms[1]);
					k.sendGroupMSG(qq, g, "清除节点“"+ms[1]+"”完毕。", false);
					save();//及时保存
					k.sendGroupMSG(qq, g, "保存完毕", false);
				}else {
					k.sendGroupMSG(qq, g, "啊偶，并没有这个节点呢...", false);
				}
				
			}catch (Exception e) {
				e.printStackTrace();
				k.sendGroupMSG(qq, g, "\\n用法:清除节点 +空格+节点的问句", true);
			}
		}else
		
		if(m.contains("自闭")) {
			
			k.CheckPermission(g, myself, "0");
			k.CheckPermission(g, qq, "0");
			
			new Thread(()->{//防止阻塞状态更新
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(isManager);
				
				if(!isManager) {
//					k.sendGroupMSG(qq, g, " 抱歉，我还不是管理员，没法给您想要的自闭呢", true);
					return;
				}else{
					switch(permission) {
						case 1:k.setForbiddenWords(qq, g, "1200");break;
						default:k.sendGroupMSG(qq, g, " 抱歉，以您尊贵的管理员身份我无法给您想要的自闭呢", true);
					}
				}
			}).start();
			
				
		}else
			
		if(m.contains("领取套餐")) {
			k.CheckPermission(g, myself, "0");
			k.CheckPermission(g, qq, "0");
			
			new Thread(()->{//防止阻塞状态更新
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(isManager);
				
				if(!isManager) {
//					k.sendGroupMSG(qq, g, " 抱歉，我还不是管理员，没法给您想要的套餐呢", true);
					return;
				}else{
					if(permission == 1) {
						try {
							String ms[] = m.split(" ",2);
							if(ms[1].equals("")||ms[0].equals("")) {
								return;//懒
							}
							int num = Integer.parseInt(ms[1]);//判断是否为数字
							if(num>2505600) {
								k.sendGroupMSG(qq, g, "这，已经是人家的极限了啦！！", true);
								k.setForbiddenWords(qq, g, "2505600");
								return;
							}
							k.setForbiddenWords(qq, g, ms[1]);
						}catch(Exception e) {
							k.setForbiddenWords(qq, g, "300");
							k.sendGroupMSG(qq, g, "唔..听不懂你在说什么呢，不然给你5分钟好啦~顺便告诉你用法噢：领取套餐+<空格>+<时长>，60是一分钟噢，120两分钟，自己找规律叭！", true);
						}
					}else {
						k.sendGroupMSG(qq, g, " 抱歉，以您尊贵的管理员身份我无法给您想要的套餐呢", true);
					}
				}
			}).start();
			
		}else 
		
		if(m.contains("语音转换")) {
			try {
				String ms[] = m.split(" ",2);
				if(ms[1].equals("")||ms[0].equals("")) {
					return;//懒
				}
				String res = "http://tts.baidu.com/text2audio?tex="+
			URLEncoder.encode(ms[1],"UTF-8")+
			"&cuid=baike&lan=ZH&ctp=1&pdt=301&vol=9&rate=32&per="+r.nextInt(12);
				k.sendGroupMSG(qq, g, " 您要的语音，请拿好:"+res, true);
				k.sendGroupMSG("", g, "如果需要压缩请点击:http://sina.lt(我才懒得帮你压缩呢)", false);
			}catch(Exception e) {
				k.sendGroupMSG(qq, g, "用法是:  语音转换+空格+<您要转换的内容> 呢", true);
			}
		}else
			
		if(m.contains("百度")) {
			
			try {
				String ms[] = m.split(" ",2);
				if(ms[1].equals("")||ms[0].equals("")) {
					return;//懒
				}
				if(ms[1].contains("[CQ")) {
					k.sendGroupMSG(qq, g,"这个，这个人家不能帮你百度的啦...",true);
				}
				String res = "https://u.iheit.com/baidu/index.html?"+
			URLEncoder.encode(ms[1],"UTF-8");
				k.sendGroupMSG(qq, g," 真是的，还要人家帮你百度，哼..自己点开看了啦!." + res, true);
			}catch(Exception e) {
				k.sendGroupMSG(qq, g, "用法是:  百度+空格+<您要搜索的内容> 呢", true);
			}
			
		}else
			
		if(m.contains("娇喘")) {
			k.sendGroupMSG(qq, g, "[CQ:record,file=2.mp3,magic=false]", false);
		}else
		
		if(m.equals("来点色图")) {//TODO
			if(r.nextInt(10)==1) {
				k.sendGroupMSG(qq, g, "[CQ:image,file=setu.jpg]", false);
				return;
			}
			if(!hasLooked.containsKey(g)) {
				hasLooked.put(g, 1);
			}else {
				hasLooked.put(g, hasLooked.get(g)+1);
			}
			if(hasLooked.get(g)==10) {
				k.sendGroupMSG(qq, g, " 不行！今天已经达到上限了啦！要注意身体！", true);
				return;
			}
			String tip = " 您要的色图，请拿好。每日上限10张噢，注意身体！[图库来源:3244247360]";
			int index = r.nextInt(1001)+1;
			k.sendGroupMSG(qq, g, tip+"[CQ:image,file="+index+".jpg]", false);
			
		}else
		
		if(m.contains("[CQ:record")) {
			k.sendGroupMSG(qq, g, " 哇！您的声音太好听了啦！", true);
		}else
			
		if(m.contains("机器人")&&r.nextInt(5)==0) {
			k.sendGroupMSG(qq, g, "[CQ:record,file=1.mp3,magic=false]", false);
			k.sendGroupMSG(qq, g, "[CQ:record,file=sq.mp3,magic=false]", false);
			cntOfAngry = 0;
		}else
			
		if(m.contains("复读次数查询")) {
			k.sendGroupMSG(qq, g, "\\n今日"+getRereadList(g,qq,m.contains("详细"))+"\\n"
					+ "每天0点清空噢,我重启的时候也会清空噢", true);
		}else
			
		if(m.contains("-version")) {
			String res = "LuLoBot(LunaLovegood Bot) version:“"+version+"”\\n";
			res += "LuLoBot(TM) (build "+version+"-bll)\\n";
			res += "Copyright 2020 GayLordFocker 版权所有\\n";
			res += "催更方式:gaylordfocker@foxmail.com";
			k.sendGroupMSG(qq, g, res, false);
		}else
			
		if(m.contains("词库查询")&&managers.contains(qq)) {
			
			if(!m.contains("详细")) {
				k.sendGroupMSG(qq, g, "可以回答的问句:"+question.toString(), false);
				return;
			}
			try {
				Scanner in = new Scanner(new File("data.xml"));
				int i=0;
				String ans = "";
				while(in.hasNextLine()) {
					ans+=in.nextLine()+"\\n";
					i++;
					if(i==20) {
						k.sendGroupMSG(qq, g, replaceNewLine(ans).replaceAll("\"", "'"), false);//量太大无法输出
						ans = "";
						i=0;
					}
				}
				k.sendGroupMSG(qq, g, replaceNewLine(ans).replaceAll("\"", "'"), false);
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				k.sendGroupMSG(qq, g, "异常！", false);
			}
			
		}else
			
		if(m.contains("问句查询")&&managers.contains(qq)) {
			try {
				String ms[] = m.split(" ",2);
				if(ms[1].equals("")||ms[0].equals("")) {
					return;//懒
				}
				if(!question.contains(ms[1])) {
					k.sendGroupMSG(qq, g, "啊偶，没用这个问句呢...", false);
					return;
				}
				Preferences node = root.node(ms[1]);
				String ans = "问句"+ms[1]+"的答语有：";
				for(int i = 1;i<=node.getInt("length", 0);i++) {
					ans+="\\n"+i+":"+node.get(i+"", "异常！");
				}
				k.sendGroupMSG(qq, g, ans, false);
			}catch(Exception e) {
				k.sendGroupMSG(qq, g, "用法是:  问句查询+空格+<问句> 呢", true);
			}
		}else
		
		if(m.contains("-help")) {
			
			String ans1 = "v"+version + "\\n版本特点：更新了未知的bug\\n带有“$”表示仅限机器人管理员使用\\n"
					+ "可用命令：\\n清除节点+问句 -清除某个问句$；\\n黑化/娘化 -黑化后将无脑骂人$；\\n发骚/停止发骚 -发骚后被艾特将说骚话，以及用骚话水群$；"
					+ "\\n(详细)词库查询 -打印所有问句,若带有‘详细’则打印整个词库$；\\n问句查询+问句 -打印该问句所有答语$"
					+ "\\n百度 -帮您百度~"
					+ "\\n语音转换 -打印百度tts转换后的语音链接；\\n自闭 -你试试就知道了（需要群管理员权限）；\\n领取套餐+<空格>+数字 -你试试就知道了（需要群管理员权限）；"
					+ "\\n(详细)复读次数查询 -打印当天此群复读次数,若带有‘详细’则打印复读内容时间以及参与人员；"
					+ "\\n-help -打印此列表\\n-version -查看当前机器人版本"
					+ "\\n成绩查询 -仅限用于xxxx最新考试成绩，当前数据为：xxx"
					+ "\\n来点色图 -哎呀羞死人了！"
					+ "\\n赞我 -给你名片点赞！"
					+ "\\n\\n注意：复读期间不会处理命令噢，有关时间的算法以服务器时间为准\\n";//TODO 更新成绩
			
			k.sendGroupMSG("", g, ans1, false);

			k.sendGroupMSG(qq, g, "调教用法：\\n"
					+ "调教+换行+问句+换行+答语。例如：\\r\\n\\r\\n"
					+ "调教\\n"
					+ "[CQ:at,qq="+myself+"] 你是\\n"
					+ "我不是机器人\\r\\n\\r\\n"
					+ "一个问句可以调教多次噢", true);
			k.sendGroupMSG("", g, "Copyright-2020-GaylordFocker版权所有", false);
		}else
		
		if(m.contains("赞我")) {
			for(int i = 0 ; i< r.nextInt(10)+1;i++) {
				k.sendPraise(qq);
			}
			
			k.sendGroupMSG(qq, g, " 操作完毕了呢", true);
		}else
			
		if(m.contains("66")||m.contains("大佬")||m.contains("膜")) {
			k.sendGroupMSG(qq, g, "[CQ:image,file=mo.jpg]", false);
		}else
			
		if((m.contains("开车")||(m.contains("福利"))&&r.nextBoolean())) {
			k.sendGroupMSG(qq, g, "[CQ:image,file=kc"+(r.nextInt(6)+1)+".jpg]", false);
		}else
			
		if(m.contains("抬")&&r.nextBoolean()) {
			k.sendGroupMSG(qq, g, "[CQ:image,file=tz.jpg]", false);
		}else
			
		if(m.contains("爱")) {
			k.sendGroupMSG(qq, g, "[CQ:image,file=chi.jpg]", false);
		}else

		if(m.contains("调教")) {
			try {
				String ms[] = m.split("\r\n",3);
				
				if(!ms[0].equals("调教")||ms[1].equals("")||ms[2].equals("")) {
					throw new Exception();//懒
				}
				
				for(String s:question) {
					if((ms[1].contains(s)||s.contains(ms[1]))&&!s.equals(ms[1])) {//防止冲突
						k.sendGroupMSG(qq, g, "抱歉，我已经学过了包含“"+s+"”的问句了呢..", true);
						return;
					}
				}
				
				if(!question.contains(ms[1])) {
					question.add(ms[1]);
				}
				
				ms[2] = replaceNewLine(ms[2]);
				
				if(ms[2].contains("CQ:")) {
					ms[2]="["+ms[2]+"]";
				}
				
				int length = root.node(ms[1]).getInt("length", 0);
				root.node(ms[1]).putInt("length", length+1);
				root.node(ms[1]).put((length+1)+"", ms[2]);
				
				save();//及时保存
				k.sendPrivateMSG(owner, ms[1]+":"+ms[2]);
				k.sendGroupMSG(qq, g, " 学习并保存完毕!\\n当有人说的话包含:\\n"
						+ "“"+ms[1]+"”的时候\\n我可能会说：“"+ms[2]+"”", true);
			}catch (Exception e) {
				k.sendGroupMSG(qq, g, "调教用法：\\n"
						+ "调教+换行+问句+换行+答语。例如：\\n"
						+ "调教\\n"
						+ "[CQ:at,qq="+myself+"] 你是\\n"
						+ "我不是机器人\\r\\n\\r\\n"
						+ "一个问句可以调教多次噢", true);
			}
		}else
			
		{
			
			for(String s:list5) {
				if(m.contains(s)) {
					k.sendGroupMSG(qq, g, list[r.nextInt(list.length)], false);
					return;
				}
			}
			
			for(String s:question) {//遍历问题随机选择该问题节点内某一句话
				if(m.contains(s)) {
					int index = r.nextInt(root.node(s).getInt("length", 0))+1;
					k.sendGroupMSG(qq, g, root.node(s).get(""+index, "啥？"), false);
					return;
				}
			}
			if(m.contains("[CQ:at,qq="+myself+"]")) {
				k.sendGroupMSG(qq, g, sexygroup.contains(g)?list4[r.nextInt(list4.length)]
						:list3[r.nextInt(list3.length)], false);
			}else if(r.nextInt(100)==1/*一定概率带读*/&&!hasReread.getOrDefault(g, false)&&!m.contains("[CQ:image")) {
				k.sendGroupMSG(qq, g, m, false);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void read() throws Exception {

		if(new File("data.xml").exists()) {
			try {
				FileInputStream fi = new FileInputStream("data.xml");
				root.importPreferences(fi);
				fi.close();
				String nodes = "";
				for(String s:root.childrenNames()) {
					question.add(s);
					fi = new FileInputStream("data.xml");
					root.node(s).importPreferences(fi);
					fi.close();
					nodes+="导入节点："+s+"\\n";
				}
				k.sendPrivateMSG(owner, "共导入节点:"+nodes);
				k.sendPrivateMSG(owner, "导入data.xml");
			} catch (IOException e1) {
				k.sendPrivateMSG(owner, "导入异常！");
			} catch (InvalidPreferencesFormatException e1) {
				k.sendPrivateMSG(owner, "data文件损坏！");
			}
		}else {
			k.sendPrivateMSG(owner, "data文件不存在！");
		}
		
		k.sendPrivateMSG(owner, "读入执行完毕");
		
	}
	
	public void save() throws Exception {
		
		try {
			out = new FileOutputStream("data.xml");
			root.exportSubtree(out);
			out.close();
			k.sendPrivateMSG(owner, "导出data.xml");
		} catch (IOException | BackingStoreException e1) {
			k.sendPrivateMSG(owner, "导出数据异常!");
		}

	}
	

	@Override
	public void Re_MSG_Private(RE_MSG_Private arg0) {
		String qq = arg0.getFromqq();
		String msg = arg0.getMsg();
		
		if(msg.contains("成绩查询")) {
			try {
				String ms[] = msg.split(" ",2);
				
				if(!ms[0].equals("成绩查询")||ms[1].equals("")) {
					throw new Exception();//懒
				}
				
				if(ms[1].equals("洪昱亮")) {
					k.sendPrivateMSG(qq, "甭查了，肯定第一。");
					return;
				}
				k.sendPrivateMSG(qq, "最新一次考试成绩（xxx）查询中，请稍等...");//TODO 更新成绩
				
				String res = "@#$";
				for(int i=0;i<820;i++) {
					if(sheet.getCell(1, i).getContents().contains(ms[1])) {
						res = "";
						for(int j = 0;j<sheet.getColumns();j++) {
							res+=sheet.getCell(j, 1).getContents()+":"+sheet.getCell(j, i).getContents()+"\\n";
						}
						k.sendPrivateMSG(qq, res);
					}
				}
				if(res.equals("@#$")) {
					k.sendPrivateMSG(qq, " 啊偶，找不到这个人呢...");
				}
				
			}catch(Exception e) {
				k.sendPrivateMSG(qq, " 用法:\\n成绩查询+<空格>+姓名（可根据姓名中的字配对）");
			}
			return;
		}else
		
		for(String s:list5) {
			if(msg.contains(s)) {
				k.sendPrivateMSG(qq, list[r.nextInt(list.length)]);
				return;
			}
		}
		
		for(String s:question) {
			if(msg.contains(s)) {
				int index = r.nextInt(root.node(s).getInt("length", 0))+1;
				k.sendPrivateMSG(qq, root.node(s).get(""+index, "啥？"));
				return;
			}
		}
		
		if(qq.equals(owner)) {
			if(msg.contains("[CQ")) {
				k.sendPrivateMSG(qq, msg.substring(0, msg.length()-1));
			}else
			if(msg.contains("保存词库")) {
				try {
					save();
					k.sendPrivateMSG(qq, "操作完毕。");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}else
			if(msg.contains("词库查询")) {
				
				try {
					Scanner in = new Scanner(new File("data.xml"));
					int i=0;
					String ans = "";
					while(in.hasNextLine()) {
						ans+=in.nextLine()+"\\n";
						i++;
						if(i%20 == 0) {
							k.sendPrivateMSG(qq, replaceNewLine(ans).replaceAll("\"", "'"));//量太大无法输出
							ans = "";
						}
					}
					k.sendPrivateMSG(qq, replaceNewLine(ans).replaceAll("\"", "'"));
					
					in.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					k.sendPrivateMSG(qq, "异常！");
				}
			}else
			if(msg.contains("换行测试")) {
				k.sendPrivateMSG(qq, "测\\n试");
			}else
			if(msg.contains("添加管理")) {
				try {
					String beOptQQ = msg.split(" ")[1];
					managers.add(beOptQQ);
					k.sendPrivateMSG(qq, "添加成功,当前管理有："+managers);
					k.sendPrivateMSG(beOptQQ, "新管理您好呀~");
				}catch(Exception e) {
					k.sendPrivateMSG(qq, "用法:添加管理 + 空格 + <管理员qq>");
				}
			}else if(msg.contains("移除管理")){
				try {
					managers.remove(msg.split(" ")[1]);
					k.sendPrivateMSG(qq, "移除成功,当前管理有："+managers);
					k.sendPrivateMSG(msg.split(" ")[1], "您被撤销了管理");
				}catch(Exception e) {
					k.sendPrivateMSG(qq, "用法:移除管理 + 空格 + <管理员qq>");
				}
			}else if(msg.equals("关闭")) {
				isAlive = false;
				k.sendPrivateMSG(qq, "关闭成功");
			}else if(msg.equals("启动")) {
				isAlive = true;
				k.sendPrivateMSG(qq, "准备开骂");
			}else if(msg.contains("移除不挨骂的群")){
				try {
					managers.remove(msg.split(" ")[1]);
					k.sendPrivateMSG(qq, "移除成功,当前不挨骂的群有："+optgroup);
				}catch(Exception e) {
					k.sendPrivateMSG(qq, "用法:移除不挨骂的群 + 空格 + <群号>");
				}
			}else if(msg.contains("添加不挨骂的群")){
				try {
					managers.remove(msg.split(" ")[1]);
					k.sendPrivateMSG(qq, "添加成功,当前不挨骂的群有："+optgroup);
				}catch(Exception e) {
					k.sendPrivateMSG(qq, "用法:添加不挨骂的群 + 空格 + <群号>");
				}
			}else if(msg.contains("状态报告")) {
				k.sendPrivateMSG(qq, "当前挨骂的群有："+optgroup+";\\n当前管理有："+managers+";\\n"
						+ "可回答的问题有："+question);
			}else if(msg.contains("启用艾特")){
				reAt = true;
				k.sendPrivateMSG(qq, "已启用骂人时艾特");
			}else if(msg.contains("紧用艾特")){
				reAt = false;
				k.sendPrivateMSG(qq, "已禁用骂人时艾特");
			}else if(msg.contains("帮助")){
				k.sendPrivateMSG(qq, "可用命令：\\n状态报告；\\n启用/禁用艾特；\\n添加/移除不挨骂的群；"
						+ "\\n启动；\\n关闭；\\n添加/移除管理；\\n帮助；\\n换行测试；\\n保存词库;\\n词库查询");
			}else {
				k.sendPrivateMSG(qq, "无法识别的命令");
			}
		}else {
			k.sendPrivateMSG(qq, list3[r.nextInt(list3.length)]);
		}
		
	}
	
	public static void main(String[] args) {
			try {
				new QQrobot(new URI("wx://localhost:25303"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
	}

}
