package com.oasisfeng.nevo.decorators.wechat;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O_MR1;

/**
 * Static map for WeChat Emoji markers
 *
 * Created by Oasis on 2018-8-9.
 */
class EmojiMap {

	// Pull Request is welcome. Please describe how to verify the related emoji in the pull request.
	// Proper emoji is not found for lines commented out. If you have good candidate, please let us know.
	// Columns are split by "tab" for visual alignment
	static final String[][] MAP = new String[][] {
			{ "OK",			"OK",			"👌" },
			{ "耶",			"Yeah!",		"✌" },
			{ "歐耶",		null,			"✌" }, // TW
			{ "嘘",			"Shhh"	,		"🤫" },
			{ "晕",			"Dizzy",		"😵" },
			{ null,			"Nuh-uh",		"🙅" },
			{ "衰",			"BadLuck",		"😳" },
			{ null,			"Toasted",		"😳" }, // same as above in newer versions
			{ "色",			"Drool",		"🤤" },
			{ "囧",			"Tension",		"😳" },
			{ null,			"Blush",		"😳" }, // same as above in newer versions
			{ "鸡",			"Chick",		"🐥" },
			{ "小雞",		null,			"🐥" }, // TW
			{ "强",			"Thumbs Up",	"👍" },
			{ null,			"ThumbsUp",		"👍" }, // same as above in newer versions
			{ "弱",			"Weak",			"👎" },
			{ null,			"ThumbsDown",	"👎" }, // same as above in newer versions
			{ "睡",			"Sleep",		"😴" },
			{ "吐",			"Puke",			"🤢" },
			{ "困",			"Drowsy",		"😪" },
			{ "發",			"Rich",			"🀅" },
			{ "微笑",		"Smile",		"🙂" },
			{ "撇嘴",		"Grimace",		"😖" },
			{ "发呆",		"Scowl",		"😳" },
			{ "得意",		"CoolGuy",		"😎" },
			{ "流泪",		"Sob",			"😭" },
			{ "害羞",		"Shy",			"☺" },
			{ "闭嘴",		"Shutup",		"🤐" },
			{ null,			"Silent",		"🤐" }, // same as above in newer versions
			{ "大哭",		"Cry",			"😢" },
			{ "尴尬",		"Awkward",		"😰" },
			{ "发怒",		"Angry",		"😡" },
			{ "调皮",		"Tongue",		"😜" },
			{ "呲牙",		"Grin",			"😁" },
			{ "惊讶",		"Surprise",		"😲" },
			{ "难过",		"Frown",		"🙁" },
			{ "抓狂",		"Scream",		"😫" },
			{ "偷笑",		"Chuckle",		"🤭" },
			{ "愉快",		"Joyful",		"☺" },
			{ "白眼",		"Slight",		"🙄" },
			{ "傲慢",		"Smug",			"😕" },
			{ "惊恐",		"Panic",		"😱" },
			{ "流汗",		"Sweat",		"😓" },
			{ "憨笑",		"Laugh",		"😄" },
			{ "悠闲",		"Loafer",		"😌" },
			{ "奋斗",		"Strive",		"💪" },
			{ "咒骂",		"Scold",		"🤬" },
			{ "疑问",		"Doubt",		"🤨" },
			{ null,			"Shocked",		"🤨" }, // same as above for newer versions
			{ "骷髅",		"Skull",		"💀" },
			{ "敲打",		"Hammer",		"👊" },
			{ "捂脸",		"Facepalm",		"🤦" },
			{ "掩面",		null,			"🤦" }, // TW
			{ "奸笑",		"Smirk",		"😏" },
			{ "皱眉",		"Concerned",	"😟" },
			{ "皺眉",		null,			"😟" }, // TW
			{ "红包",		"Packet",		SDK_INT > O_MR1 ? "🧧"/* Emoji 11+ */: "💰" },
			{ "小狗",		"Pup",			"🐶" },
			{ "再见",		"Bye",			"🙋" },
			{ "再見",		null,			"🙋" }, // TW
			{ null,			"Wave",			"🙋" }, // same as above
			{ "擦汗",		"Relief",		"😶" },
			{ null,			"Speechless",	"😶" }, // same as above in newer versions
			{ "鼓掌",		"Clap",			"👏" },
			{ "坏笑",		"Trick",		"👻" },
			{ "哈欠",		"Yawn",			"🥱" },
			{ "鄙视",		"Lookdown",		"😒" },
			{ null,			"Pooh-pooh",	"😒" }, // same as above for newer wechat
			{ "委屈",		"Wronged",		"😞" },
			{ null,			"Shrunken",		"😞" },
			{ "阴险",		"Sly",			"😈" },
			{ "亲亲",		"Kiss",			"😘" },
			{ "菜刀",		"Cleaver",		"🔪" },
			{ "西瓜",		"Melon",		"🍉" },
			{ "啤酒",		"Beer",			"🍺" },
			{ "咖啡",		"Coffee",		"☕" },
			{ "猪头",		"Pig",			"🐷" },
			{ "玫瑰",		"Rose",			"🌹" },
			{ "凋谢",		"Wilt",			"🥀" },
			{ "嘴唇",		"Lip",			"👄" },
			{ null,			"Lips",			"👄" }, // same as above for newer wechat
			{ "爱心",		"Heart",		"❤" },
			{ "心碎",		"BrokenHeart",	"💔" },
			{ "蛋糕",		"Cake",			"🎂" },
			{ "炸弹",		"Bomb",			"💣" },
			{ "便便",		"Poop",			"💩" },
			{ "月亮",		"Moon",			"🌙" },
			{ "太阳",		"Sun",			"🌞" },
			{ "拥抱",		"Hug",			"🤗" },
			{ "握手",		"Shake",		"🤝" },
			{ "胜利",		"Victory",		"✌" },
			{ null,			"Peace",		"✌" }, // same as above in newer versions
			{ "抱拳",		"Salute",		"🙏" },
			{ null,			"Fight",		"🙏" }, // same as above
			{ "拳头",		"Fist",			"✊" },
//			{ "跳跳",		"Waddle",		"" },
			{ "发抖",		"Tremble",		"🥶" },
			{ "怄火",		"Aaagh!",		"😡" },
//			{ "转圈",		"Twirl",		"" },
			{ "蜡烛",		"Candle",		"🕯️" },
			{ "蠟燭",		null,			"🕯️" }, // TW
//			{ "勾引",		"Beckon",		""},
//			{ "嘿哈",		"Hey",			"" },
//			{ "吼嘿",		null,			"" }, // TW
			{ "机智",		"Smart",		"👉" },
			{ "機智",		null,			"👉" }, // TW
//			{ "抠鼻",		"DigNose",		"" },
//			{ null,			"NosePick",		"" }, // same as above for newer wechat
			{ "可怜",		"Whimper",		"🥺" },
			{ "快哭了",		"Puling",		"😢" },
			{ null,			"TearingUp",	"😢" }, // same as above for newer wechat
			{ "左哼哼",		"Bah！L",		"😗" },
			{ "右哼哼",		"Bah！R",		"😗" },
			{ "破涕为笑",	"Lol",			"😂" },
			{ "破涕為笑",	null,			"😂" }, // TW
			{ "悠闲",		"Commando", 	"🪖" },
			{ "笑脸",		"Happy", 		"😄" },
			{ "笑臉",		null, 			"😄" }, // TW
			{ "生病",		"Sick", 		"😷" },
			{ "脸红",		"Flushed", 		"😳" },
			{ "臉紅",		null, 			"😳" }, // TW
			{ "恐惧",		"Terror", 		"😱" },
			{ "恐懼",		null, 			"😱" }, // TW
			{ "失望",		"LetDown",	 	"😔" },
			{ null,			"Let Down",	 	"😔" },
			{ "无语",		"Duh", 			"😒" },
			{ "無語",		null, 			"😒" }, // TW
			{ "吃瓜",		"Onlooker", 	"🍉" },
			{ "加油",		"GoForIt", 		"✊" },
			{ "加油加油",	"KeepFighting", "😷" },
			{ "加油！",		null,			"😷" }, // TW
			{ "汗",			"Sweats", 		"😑" },
			{ "天啊",		"OMG", 			"🤯" },
//			{ "一言難盡",	"Emm", 			"" },
			{ "社会社会",	"Respect", 		"👏" },
			{ "失敬失敬",	null, 			"👏" }, // TW
			{ "旺柴",		"Doge", 		"🐶" },
			{ "好的",		"NoProb", 		"👌" },
			{ "打脸",		"MyBad", 		"👊" },
			{ "打臉",		null, 			"👊" }, // TW
			{ "哇",			"Wow", 			"🤩" },
			{ "翻白眼",		"Boring", 		"🙄" },
			{ "666",		"Awesome", 		"😝" },
//			{ "让我看看",	"LetMeSee", 	"" },
//			{ "讓我看看",	null, 			"" }, // TW
			{ "叹气",		"Sigh", 		"😌" }, // will have its own in next standard => 😮‍💨
			{ "嘆息",		null, 			"😌" }, // TW
			{ "苦涩",		"Hurt", 		"😥" },
			{ "難受",		null, 			"😥" }, // TW
			{ "裂开",		"Broken", 		"💔" },
			{ "崩潰",		null, 			"💔" }, // TW
			{ "合十",		"Worship",		"🙏" },
			{ "福",			"Blessing",		"🌠" }, //wishing star is often used as a "blessing" or "wish"
			{ "烟花",		"Fireworks",	"🎆" },
			{ "煙花",		null,			"🎆" }, // TW
			{ "爆竹",		"Firecracker",	"🧨" },

			// regular ones, usually can be found in titles
			{ null,			"Guitar",		"🎸" },
			{ null,			"Noodles",		"🍜" },
			{ null,			"Singing",		"🎤" },
			{ null,			"Fire",			"🔥" },
			{ null, 		"Big Smile",	"😃" },
			{ null,			"Glowing",		"☺" },
			{ null,			"Satisfied",	"😌" },
			{ null,			"Wink",			"😉" },
			{ null,			"Tease",		"😜" },
			{ null,			"Upset",		"😠" },
			{ null,			"Worried",		"😰" },
			{ null,			"Tear",			"😢" },
			{ null,			"Dead",			"😲" },
			{ null,			"Anxious",		"😥" },
			{ null,			"Low",			"😞" },
			{ null,			"Ugh",			"😖" },
			{ null,			"D’oh!",		"😣" },
			{ null,			"Zzz",			"😪" },
			{ null,			"Ghost",		"👻" },
			{ null,			"Alien",		"👽" },
			{ null,			"Jack-o-lantern", "🎃" },
			{ null,			"Demon",		"👿" },
			{ null,			"Star",			"🌟" },
			{ null,			"Twinkle",		"✨" },
			{ null,			"Drops",		"💦" },
			{ null,			"Asleep",		"💤" },
			{ null,			"Cupid",		"💘" },
			{ null,			"Candy Box",	"💝" },
			{ null,			"Heartbroken",	"💔" },
			{ null,			"Thumbs Down",	"👎" },
			{ null,			"High-five",	"🙌" },
			{ null,			"Fist Bump",	"👊" },
			{ null,			"Strong",		"💪" },
			{ null,			"#1",			"☝" },
			{ null,			"Up",			"👆" },
			{ null,			"Down",			"👇" },
			{ null,			"Right",		"👉" },
			{ null,			"Left",			"👈" },
			{ null,			"Wash",			"🛁" },
			{ null,			"Xmas Tree",	"🎄" },
			{ null,			"Angel",		"👼" },
			{ null,			"Plane",		"✈" },
			{ null,			"Dance",		"💃" },
			{ null,			"Flower",		"🌺" },
			{ null,			"Cactus",		"🌵" },
			{ null,			"Palm",			"🌴" },
			{ null,			"Waves",		"🌊" },
			{ null,			"Snowman",		"⛄" },
			{ null,			"Cloud",		"☁" },
			{ null,			"Rain",			"☔" },
			{ null,			"Monkey",		"🐵" },
			{ null,			"Tiger",		"🐯" },
			{ null,			"Cat",			"🐱" },
			{ null,			"Doggy",		"🐶" },
			{ null,			"Dog",			"🐺" },
			{ null,			"Bear",			"🐻" },
			{ null,			"Koala",		"🐨" },
			{ null,			"Hamster",		"🐹" },
			{ null,			"Mouse",		"🐭" },
			{ null,			"Rabbit",		"🐰" },
			{ null,			"Cow",			"🐮" },
			{ null,			"Boar",			"🐗" },
			{ null,			"Horse",		"🐴" },
			{ null,			"Frog",			"🐸" },
			{ null,			"Snake",		"🐍" },
			{ null,			"Chicken",		"🐔" },
			{ null,			"Pigeon",		"🐦" },
			{ null,			"why",			"❔" },
			{ null,			"exclamation",	"❕" },
			{ null,			"Warning",		"⚠" },
			{ null,			"Music",		"🎵" },
			{ null,			"Punch",		"👊" },
//			{ null,			"Kissing",		"" }, // not sure
//			{ null,			"Couple",		"" }, //could't find it
			{ null,			"Boy",			"👦" },
			{ null,			"Girl",			"👧" },
			{ null,			"Lady",			"👩" },
			{ null,			"Man",			"👨" },
			{ null,			"Penguin",		"🐧" },
			{ null,			"Caterpillar",	"🐛" },
			{ null,			"Octopus",		"🐙" },
			{ null,			"Fish",			"🐟" },
			{ null,			"Whale",		"🐳" },
			{ null,			"Dolphin",		"🐬" },
			{ null,			"Santa",		"🎅" },
			{ null,			"Bell",			"🔔" },
			{ null,			"Balloon",		"🎈" },
			{ null,			"CD",			"💿" },
			{ null,			"Film Camera",	"🎥" },
			{ null,			"Computer",		"💻" },
			{ null,			"TV",			"📺" },
			{ null,			"Phone",		"📱" },
			{ null,			"Unlocked",		"🔓" },
			{ null,			"Locked",		"🔒" },
			{ null,			"Key",			"🔑" },
//			{ null,			"Judgement",	"" }, // unsure of this one
			{ null,			"Light bulb",	"💡" },
			{ null,			"Mail",			"📫" },
			{ null,			"Pistol",		"🔫" },
			{ null,			"Soccer Ball",	"⚽" },
			{ null,			"Golf",			"⛳" },
			{ null,			"Trophy",		"🏆" },
			{ null,			"Invader",		"👾" },
			{ null,			"Bikini",		"👙" },
			{ null,			"Crown",		"👑" },
			{ null,			"Umbrella",		"☂" },
			{ null,			"Purse",		"👛" },
			{ null,			"Lipstick",		"💄" },
			{ null,			"Ring",			"💍" },
//			{ null,			"Toast",		"" }, //not sure which one this is
			{ null,			"Martini",		"🍸" },
			{ null,			"Burger",		"🍔" },
			{ null,			"Fries",		"🍟" },
			{ null,			"Sphaghetti",	"🍝" }, //yes, the original is mispelled!
			{ null,			"Spaghetti",	"🍝" }, // add in the fixed one in case they change it
			{ null,			"Sushi",		"🍣" },
			{ null,			"Eggs",			"🥚" },
			{ null,			"Ice Cream",	"🍦" },
			{ null,			"Apple",		"🍎" },
			{ null,			"Bike",			"🚲" },
			{ null,			"Bullet Train",	"🚅" },
			{ null,			"Flag",			"🏁" },
			{ null,			"Men",			"🚹" },
			{ null,			"Women",		"🚺" },
			{ null,			"O",			"⭕" },
			{ null,			"X",			"❌" },
			{ null,			"Copyright",	"©" },
			{ null,			"Registered TM","®" },
			{ null,			"Trademark",	"™" },

					// From WeChat for iOS
			{ "强壮",		null,			"💪"},
			{ "鬼魂",		null,			"👻"},

			// From WeChat for PC
			{ "篮球",		"Basketball",	"🏀" },
			{ "乒乓",		"PingPong",		"🏓" },
			{ "饭",			"Rice",			"🍚" },
			{ "瓢虫",		"Ladybug",		"🐞" },
			{ "礼物",		"Gift",			"🎁" },
			{ "禮物",		"gift",			"🎁" }, // TW + alternate key
			{ null,			"Watermelon",	"🍉" },
//			{ "差劲",		"Pinky",		"" },
			{ "爱你",		"RockOn",		"🤟" },
			{ null,			"Love",			"😍" },
			{ null,			"NO",			"🙅" },
			{ "爱情",		"InLove",		"💕" },
			{ "飞吻",		"Blowkiss",		"😘" },
			{ "闪电",		"Lightning",	"⚡" },
			{ "刀",			"Dagger",		"🗡️" },		// Dup of "Cleaver"
			{ "足球",		"Soccer",		"⚽" },
			{ "棒球",		"Baseball",		"⚾" },
			{ "橄榄球",		"Football",		"🏈" },
			{ "钱",			"Money",		"💰" },
			{ "相机",		"Camera",		"📷" },
			{ "干杯",		"Cheers",		"🍻" },
			{ "宝石",		"Gem",			"💎" },
			{ "茶",			"Tea",			"🍵" },
			{ "药丸",		"Pill",			"💊" },
			{ "庆祝",		"Party",		"🎉" },
			{ "慶祝",		null,			"🎉" }, // TW
			{ "火箭",		"Rocket ship",	"🚀" },
			{ "饥饿",		"Hungry", 		"😋" },
			{ "酷",			"Ruthless", 	"😈" },
			{ "吓",			"Uh Oh", 		"😠" },
			{ null,			"Wrath", 		"😠" }, // Dup of above
			{ "奋斗",		"Determined", 	"😣" },
			{ "疯了",		"Tormented", 	"😤" },
			{ "糗大了",		"Shame", 		"😳" },
			{ "磕头",		"Kotow",		"🙇" },
			{ "回头",		"Lookback",		"🤚" },
//			{ "跳绳",		"Jump",			"" },
//			{ null,			"JumpRope",		"" }, // dup
			{ "投降",		"Surrender",	"🏳️" },
			{ "激动",		"Hooray",		"🙌" },
//			{ "乱舞",		"HeyHey",		"" },
			{ "献吻",		"Smooch",		"😘" },
//			{ "左太极",		"TaiJi L",		"" },
//			{ null,			"TaiChi L",		"" }, //dup
//			{ "右太极",		"TaiJi R",		"" },
//			{ null,			"TaiChi R",		"" }, //dup
			{ null,			"Dramatic",		"🤚" }, // back of the hand
//			{ null,			"Meditate",		"" },
	};
}
