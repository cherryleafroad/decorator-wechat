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
			{ "成交",		"OK",			"👌" },
			{ "ตกลง",		null,			"👌" }, // TH
			{ "耶",			"Yeah!",		"✌" },
			{ "歐耶",		null,			"✌" }, // TW
			{ "嘘",			"Shhh",			"🤫" },
			{ "จุ๊ๆ",			null,			"🤫" }, // TH
			{ "噓",			null,			"🤫" }, // TW
			{ "晕",			"Dizzy",		"😵" },
			{ "เวียนหัว",		null,			"😵" }, // TH
			{ "暈",			null,			"😵" }, // TW
			{ null,			"Nuh-uh",		"🙅" },
			{ "衰",			"BadLuck",		"😳" },
			{ null,			"Toasted",		"😳" }, // same as above in newer versions
			{ "ชั่วร้าย",		null,			"😳" }, // TH
			{ "色",			"Drool",		"🤤" },
			{ "น้ำลายไหล",	null,			"🤤" },
			{ "囧",			"Tension",		"😳" },
			{ null,			"Blush",		"😳" }, // same as above in newer versions
			{ "เขินอาย",		null,			"😳" }, // TH
			{ "鸡",			"Chick",		"🐥" },
			{ "小雞",		null,			"🐥" }, // TW
			{ "强",			"Thumbs Up",	"👍" },
			{ "ยอดเยี่ยม",		null,			"👍" }, // TH
			{ null,			"ThumbsUp",		"👍" }, // same as above in newer versions
			{ "弱",			"Weak",			"👎" },
			{ null,			"ThumbsDown",	"👎" }, // same as above in newer versions
			{ "ยอดแย่",		null,			"👎" }, // TH
			{ "睡",			"Sleep",		"😴" },
			{ "หลับ",		null,			"😴" }, // TH
			{ "睡覺",		null,			"😴" }, // TW
			{ "吐",			"Puke",			"🤢" },
			{ "อาเจียน",		null,			"🤢" }, // TH
			{ "困",			"Drowsy",		"😪" },
			{ "ง่วงนอน",		null,			"😪" }, // TH
			{ "累",			null,			"😪" }, // TW
			{ "發",			"Rich",			"🀅" },
			{ "微笑",		"Smile",		"🙂" },
			{ "ยิ้ม",			null,			"🙂" }, // TH
			{ "撇嘴",		"Grimace",		"😖" },
			{ "หน้าบูด",		null,			"😖" }, // TH
			{ "发呆",		"Scowl",		"😳" },
			{ "หน้าบึ้ง",		null,			"😳" }, // TH
			{ "發呆",		null,			"😳" }, // TW
			{ "得意",		"CoolGuy",		"😎" },
			{ "สบาย",		null,			"😎" }, // TH
			{ "流泪",		"Sob",			"😭" },
			{ "ร้องไห้โฮ",		null,			"😭" }, // TH
			{ "流淚",		null,			"😭" }, // TW
			{ "害羞",		"Shy",			"☺" },
			{ "อาย",		null,			"☺" }, // TH
			{ "冷汗",		null,			"☺" }, // TW
			{ "闭嘴",		"Shutup",		"🤐" },
			{ "閉嘴",		"Silent",		"🤐" }, // same as above in newer versions
			{ "ห้ามพูด",		null,			"🤐" }, // TH
			{ "大哭",		"Cry",			"😢" },
			{ "ร้องไห้",		null,			"😢" }, // TH
			{ "哭",			null,			"😢" }, // TW
			{ "尴尬",		"Awkward",		"😰" },
			{ "ลำบากใจ",		null,			"😰" }, // TH
			{ "尷尬",		null,			"😰" }, // TW
			{ "发怒",		"Angry",		"😡" },
			{ "โกรธสุด",		null,			"😡" }, // TH
			{ "發怒",		"生氣",			"😡" }, // TW
			{ "调皮",		"Tongue",		"😜" },
			{ "ขยิบตา",		null,			"😜" }, // TH
			{ "調皮",		"吐舌",			"😜" }, // TW
			{ "呲牙",		"Grin",			"😁" },
			{ "ยิ้มกว้าง",		null,			"😁" }, // TH
			{ "露齒笑",		null,			"😁" }, // TW
			{ "惊讶",		"Surprise",		"😲" },
			{ "ประหลาดใจ",	null,			"😲" }, // TH
			{ "驚訝",		null,			"😲" }, // TW
			{ "难过",		"Frown",		"🙁" },
			{ "เสียใจ",		null,			"🙁" }, // TH
			{ "難過",		null,			"🙁" }, // TW
			{ "抓狂",		"Scream",		"😫" },
			{ "กรีดร้อง",		null,			"😫" }, // TH
			{ "偷笑",		"Chuckle",		"🤭" },
			{ "หัวเราะหึๆ",	null,			"🤭" }, // TH
			{ "愉快",		"Joyful",		"☺" },
			{ "พอใจ",		null,			"☺" }, // TH
			{ "白眼",		"Slight",		"🙄" },
			{ "สงสัย",		null,			"🙄" }, // null
			{ "傲慢",		"Smug",			"😕" },
			{ "หยิ่ง",		null,			"😕" }, // TH
			{ "惊恐",		"Panic",		"😱" },
			{ "ตกใจกลัว",	null,			"😱" }, // TH
			{ "驚恐",		null,			"😱" }, // TW
			{ "流汗",		"Sweat",		"😓" },
			{ "เหงื่อตก",		null,			"😓" }, // TH
			{ "憨笑",		"Laugh",		"😄" },
			{ "หัวเราะ",		null,			"😄" }, // TH
			{ "大笑",		null,			"😄" }, // TW
			{ "悠闲",		"Loafer",		"😌" },
			{ "悠閑",		null,			"😌" }, // TW
			{ "奋斗",		"Strive",		"💪" },
			{ "มุ่งมั่น",		null,			"💪" }, // TH
			{ "奮鬥",		null,			"💪" }, // TW
			{ "咒骂",		"Scold",		"🤬" },
			{ "ด่าว่าา",		null,			"🤬" }, // TH
			{ "咒罵",		null,			"🤬" }, // TW
			{ "疑问",		"Doubt",		"🤨" },
			{ "疑問",		null,			"🤨" },
			{ "震驚",		"Shocked",		"🤨" }, // same as above for newer versions
			{ "สับสน",		null,			"🤨" }, // TH
			{ "骷髅",		"Skull",		"💀" },
			{ "หัวกะโหลก",	null,			"💀" }, // TH
			{ "骷髏",		"骷髏頭",		"💀" }, // TW
			{ "敲打",		"Hammer",		"👊" },
			{ "ค้อนทุบ",		null,			"👊" }, // TH
			{ "捂脸",		"Facepalm",		"🤦" },
			{ "掩面",		null,			"🤦" }, // TW
			{ "奸笑",		"Smirk",		"😏" },
			{ "壞笑",		null,			"😏" }, // TW
			{ "皱眉",		"Concerned",	"😟" },
			{ "皺眉",		null,			"😟" }, // TW
			{ "红包",		"Packet",		SDK_INT > O_MR1 ? "🧧"/* Emoji 11+ */: "💰" },
			{ "小狗",		"Pup",			"🐶" },
			{ "再见",		"Bye",			"🙋" },
			{ "บายๆ",		null,			"🙋" }, // TH
			{ "再見",		null,			"🙋" }, // TW
			{ null,			"Wave",			"🙋" }, // same as above
			{ "擦汗",		"Relief",		"😶" },
			{ null,			"Speechless",	"😶" }, // same as above in newer versions
			{ "เช็ดเหงื่อ",		null,			"😶" }, // TH
			{ "鼓掌",		"Clap",			"👏" },
			{ "ตบมือ",		null,			"👏" }, // TH
			{ "坏笑",		"Trick",		"👻" },
			{ "กลโกง",		null,			"👻" }, // TH
			{ "哈欠",		"Yawn",			"🥱" },
			{ "หาว",		null,			"🥱" }, // TH
			{ "鄙视",		"Lookdown",		"😒" },
			{ "鄙視",		null,			"😒" }, // TW
			{ "呸",			"Pooh-pooh",	"😒" }, // same as above for newer wechat
			{ "ดูถูก",		null,			"😒" }, // TH
			{ "委屈",		"Wronged",		"😞" },
			{ null,			"Shrunken",		"😞" },
			{ "ข้องใจ",		null,			"😞" }, // TH
			{ "阴险",		"Sly",			"😈" },
			{ "ขี้โกง",		null,			"😈" }, // TH
			{ "陰險",		null,			"😈" }, // TW
			{ "亲亲",		"Kiss",			"😘" },
			{ "จุ๊บ",			null,			"😘" }, // TH
			{ "接吻",		"親吻",			"😘" }, // TW
			{ "吻",			"獻吻",			"😘" }, // TW
			{ "親親",		null,			"😘" }, // TW
			{ "菜刀",		"Cleaver",		"🔪" },
			{ "มีด",			null,			"🔪" }, // TH
			{ "西瓜",		"Melon",		"🍉" },
			{ "啤酒",		"Beer",			"🍺" },
			{ "เบียร์",		null,			"🍺" }, // TH
			{ "咖啡",		"Coffee",		"☕" },
			{ "กาแฟ",		null,			"☕" }, // TH
			{ "猪头",		"Pig",			"🐷" },
			{ "หมู",			null,			"🐷" }, // TH
			{ "豬頭",		"豬",			"🐷" }, // TW
			{ "玫瑰",		"Rose",			"🌹" },
			{ "กุหลาบ",		null,			"🌹" }, // TH
			{ "凋谢",		"Wilt",			"🥀" },
			{ "ร่วงโรย",		null,			"🥀" }, // TH
			{ "枯萎",		null,			"🥀" }, // TW
			{ "嘴唇",		"Lip",			"👄" },
			{ "紅唇",		"Lips",			"👄" }, // same as above for newer wechat
			{ "ริมฝีปาก",		null,			"👄" }, // TH
			{ "爱心",		"Heart",		"❤" },
			{ "หัวใจ",		null,			"❤" }, // TH
			{ "心",			"愛心",			"❤" }, // TW
			{ "心碎",		"BrokenHeart",	"💔" },
			{ "ใจสลาย",		null,			"💔" }, // TH
			{ "蛋糕",		"Cake",			"🎂" },
			{ "เค้ก",			null,			"🎂" }, // TH
			{ "炸弹",		"Bomb",			"💣" },
			{ "ระเบิด",		null,			"💣" }, // TH
			{ "炸彈",		null,			"💣" }, // TW
			{ "便便",		"Poop",			"💩" },
			{ "อุจจาระ",		null,			"💩" }, // TH
			{ "月亮",		"Moon",			"🌙" },
			{ "พระจันทร์",	null,			"🌙" }, // TH
			{ "太阳",		"Sun",			"🌞" },
			{ "พระอาทิตย์",	null,			"🌞" }, // TH
			{ "太陽",		null,			"🌞" }, // TW
			{ "拥抱",		"Hug",			"🤗" },
			{ "กอด",		null,			"🤗" }, // TH
			{ "擁抱",		null,			"🤗" }, //TW
			{ "握手",		"Shake",		"🤝" },
			{ "จับมือ",		null,			"🤝" }, // TH
			{ "胜利",		"Victory",		"✌" },
			{ "สู้ตาย",		null,			"✌" }, // TH
			{ "勝利",		"Peace",		"✌" }, // same as above in newer versions
			{ "抱拳",		"Salute",		"🙏" },
			{ null,			"Fight",		"🙏" }, // same as above
			{ "คารวะ",		null,			"🙏" }, // TH
			{ "拳头",		"Fist",			"✊" },
			{ "กำหมัด",		null,			"✊" }, // TH
			{ "拳頭",		"擊拳",			"✊" }, // TW
//			{ "跳跳",		"Waddle",		"" },
			{ "发抖",		"Tremble",		"🥶" },
			{ "เขย่า",		null,			"🥶" }, // TH
			{ "發抖",		null,			"🥶" }, // TW
			{ "怄火",		"Aaagh!",		"😡" },
			{ "อ้ากส์!",		null,			"😡" }, // TH
//			{ "转圈",		"Twirl",		"" },
//			{ "หมุนตัว",		null,			"" }, // TH
//			{ "轉圈",		null,			"" }, // TW
			{ "蜡烛",		"Candle",		"🕯️" },
			{ "蠟燭",		null,			"🕯️" }, // TW
//			{ "勾引",		"Beckon",		""},
//			{ "เข้ามา",		null,			""},
//			{ "嘿哈",		"Hey",			"" },
//			{ "吼嘿",		null,			"" }, // TW
			{ "机智",		"Smart",		"👉" },
			{ "機智",		null,			"👉" }, // TW
//			{ "抠鼻",		"DigNose",		"" },
//			{ "摳鼻",		"NosePick",		"" }, // same as above for newer wechat
//			{ "แคะจมูก",		null,			"" }, // TH
			{ "可怜",		"Whimper",		"🥺" },
			{ "น่าสงสาร",		null,			"🥺" }, // TH
			{ "可憐",		null,			"🥺" }, // TW
			{ "快哭了",		"Puling",		"😢" },
			{ null,			"TearingUp",	"😢" }, // same as above for newer wechat
			{ "เกือบร้องไห้",	null,			"😢" }, // TH
			{ "左哼哼",		"Bah！L",		"😗" },
			{ "เชิดซ้าย",		null,			"😗" }, // TH
			{ "右哼哼",		"Bah！R",		"😗" },
			{ "เชิดขวา",		null,			"😗" }, // TH
			{ "破涕为笑",	"Lol",			"😂" },
			{ "破涕為笑",	null,			"😂" }, // TW
			{ "悠闲",		"Commando", 	"🪖" },
			{ "ทหาร",		null, 			"🪖" }, // TH
			{ "笑脸",		"Happy", 		"😄" },
			{ "開心",		null,			"😄" }, // TW
			{ "笑臉",		null, 			"😄" }, // TW
			{ "生病",		"Sick", 		"😷" },
			{ "脸红",		"Flushed", 		"😳" },
			{ "臉紅",		null, 			"😳" }, // TW
			{ "恐惧",		"Terror", 		"😱" },
			{ "恐懼",		null, 			"😱" }, // TW
			{ "失望",		"LetDown",	 	"😔" },
			{ "悔恨",		"Let Down",	 	"😔" }, // TW
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
			{ "吉他",		"Guitar",		"🎸" },
			{ "麵條",		"Noodles",		"🍜" },
			{ "唱歌",		"Singing",		"🎤" },
			{ "火",			"Fire",			"🔥" },
			{ null, 		"Big Smile",	"😃" },
			{ null,			"Glowing",		"☺" },
			{ "滿意",		"Satisfied",	"😌" },
			{ "眨眼",		"Wink",			"😉" },
			{ "戲弄",		"Tease",		"😜" },
			{ "心煩",		"Upset",		"😠" },
			{ "擔心",		"Worried",		"😰" },
			{ "眼淚",		"Tear",			"😢" },
			{ null,			"Dead",			"😲" },
			{ "焦慮",		"Anxious",		"😥" },
			{ "低落",		"Low",			"😞" },
			{ null,			"Ugh",			"😖" },
			{ null,			"D’oh!",		"😣" },
			{ null,			"Zzz",			"😪" },
			{ null,			"Ghost",		"👻" },
			{ "外星人",		"Alien",		"👽" },
			{ "南瓜燈",		"Jack-o-lantern", "🎃" },
			{ "惡魔",		"Demon",		"👿" },
			{ "星星",		"Star",			"🌟" },
			{ "閃爍",		"Twinkle",		"✨" },
			{ "水滴",		"Drops",		"💦" },
			{ "睡著",		"Asleep",		"💤" },
			{ "丘比特",		"Cupid",		"💘" },
			{ "禮盒",		"Candy Box",	"💝" },
			{ null,			"Heartbroken",	"💔" },
			{ null,			"Thumbs Down",	"👎" },
			{ "擊掌",		"High-five",	"🙌" },
			{ null,			"Fist Bump",	"👊" },
			{ "強",			"Strong",		"💪" },
			{ "強壯",		null,			"💪" },
			{ "第一",		"#1",			"☝" },
			{ "上",			"Up",			"👆" },
			{ "下",			"Down",			"👇" },
			{ "右",			"Right",		"👉" },
			{ "左",			"Left",			"👈" },
			{ "浴缸",		"Wash",			"🛁" },
			{ "聖誕樹",		"Xmas Tree",	"🎄" },
			{ "天使",		"Angel",		"👼" },
			{ "飛機",		"Plane",		"✈" },
			{ "跳舞",		"Dance",		"💃" },
			{ "花",			"Flower",		"🌺" },
			{ "仙人掌",		"Cactus",		"🌵" },
			{ "棕櫚樹",		"Palm",			"🌴" },
			{ "海浪",		"Waves",		"🌊" },
			{ "雪人",		"Snowman",		"⛄" },
			{ "多雲",		"Cloud",		"☁" },
			{ "下雨",		"Rain",			"☔" },
			{ "猴子",		"Monkey",		"🐵" },
			{ "老虎",		"Tiger",		"🐯" },
			{ "貓",			"Cat",			"🐱" },
			{ null,			"Doggy",		"🐶" },
			{ "狗",			"Dog",			"🐺" },
			{ "熊",			"Bear",			"🐻" },
			{ "考拉",		"Koala",		"🐨" },
			{ "倉鼠",		"Hamster",		"🐹" },
			{ "老鼠",		"Mouse",		"🐭" },
			{ "兔子",		"Rabbit",		"🐰" },
			{ "牛",			"Cow",			"🐮" },
			{ "野豬",		"Boar",			"🐗" },
			{ "馬",			"Horse",		"🐴" },
			{ "青蛙",		"Frog",			"🐸" },
			{ "蛇",			"Snake",		"🐍" },
			{ "雞",			"Chicken",		"🐔" },
			{ "鴿子",		"Pigeon",		"🐦" },
			{ "問號",		"why",			"❔" },
			{ "嘆號",		"exclamation",	"❕" },
			{ "警告",		"Warning",		"⚠" },
			{ "音樂",		"Music",		"🎵" },
			{ null,			"Punch",		"👊" },
			{ null,			"Kissing",		"💏" },
			{ null,			"Couple",		"👫" }, //could't find it
			{ "男孩",		"Boy",			"👦" },
			{ "女孩",		"Girl",			"👧" },
			{ "女士",		"Lady",			"👩" },
			{ "男士",		"Man",			"👨" },
			{ "企鵝",		"Penguin",		"🐧" },
			{ "毛蟲",		"Caterpillar",	"🐛" },
			{ "八爪魚",		"Octopus",		"🐙" },
			{ "魚",			"Fish",			"🐟" },
			{ "鯨魚",		"Whale",		"🐳" },
			{ "海豚",		"Dolphin",		"🐬" },
			{ "聖誕老人",	"Santa",		"🎅" },
			{ "鈴鐺",		"Bell",			"🔔" },
			{ "氣球",		"Balloon",		"🎈" },
			{ null,			"CD",			"💿" },
			{ "錄影機",		"Film Camera",	"🎥" },
			{ "電腦",		"Computer",		"💻" },
			{ "電視",		"TV",			"📺" },
			{ "電話",		"Phone",		"📱" },
			{ "解鎖",		"Unlocked",		"🔓" },
			{ "鎖",			"Locked",		"🔒" },
			{ "鑰匙",		"Key",			"🔑" },
//			{ "",			"Judgement",	"" }, // unsure of this one
			{ "燈泡",		"Light bulb",	"💡" },
			{ "郵箱",		"Mail",			"📫" },
			{ "手槍",		"Pistol",		"🔫" },
			{ null,			"Soccer Ball",	"⚽" },
			{ "高爾夫",		"Golf",			"⛳" },
			{ "獎盃",		"Trophy",		"🏆" },
			{ "入侵者",		"Invader",		"👾" },
			{ "比基尼",		"Bikini",		"👙" },
			{ "皇冠",		"Crown",		"👑" },
			{ "雨傘",		"Umbrella",		"☂" },
			{ "手提包",		"Purse",		"👛" },
			{ "口紅",		"Lipstick",		"💄" },
			{ "戒指",		"Ring",			"💍" },
//			{ "",			"Toast",		"" }, //not sure which one this is
			{ "雞尾酒",		"Martini",		"🍸" },
			{ "漢堡",		"Burger",		"🍔" },
			{ "薯條",		"Fries",		"🍟" },
			{ "意粉",		"Sphaghetti",	"🍝" }, //yes, the original is mispelled!
			{ null,			"Spaghetti",	"🍝" }, // add in the fixed one in case they change it
			{ "壽司",		"Sushi",		"🍣" },
			{ "煎蛋",		"Eggs",			"🍳" },
			{ "雪糕",		"Ice Cream",	"🍦" },
			{ "蘋果",		"Apple",		"🍎" },
			{ "單車",		"Bike",			"🚲" },
			{ "高鐵",		"Bullet Train",	"🚅" },
			{ "旗",			"Flag",			"🏁" },
			{ "男",			"Men",			"🚹" },
			{ "女",			"Women",		"🚺" },
			{ null,			"O",			"⭕" },
			{ null,			"X",			"❌" },
			{ "版權",		"Copyright",	"©" },
			{ "注冊商標",	"Registered TM","®" },
			{ "商標",		"Trademark",	"™" },

//			{ "熱情",		"",	"" }, // TW - Not sure which it belongs to

					// From WeChat for iOS
			{ "强壮",		null,			"💪"},
			{ "鬼魂",		null,			"👻"},

			// From WeChat for PC
			{ "篮球",		"Basketball",	"🏀" },
			{ "บาสเกตบอล",	null,			"🏀" }, // TH
			{ "籃球",		null,			"🏀" }, // TW
			{ "籃球",		null,			"🏀" },
			{ "乒乓",		"PingPong",		"🏓" },
			{ "ปิงปอง",		null,			"🏓" }, // TH
			{ "饭",			"Rice",			"🍚" },
			{ "ข้าว",		null,			"🍚" }, // TH
			{ "飯",			null,			"🍚" }, // TW
			{ "瓢虫",		"Ladybug",		"🐞" },
			{ "เต่าทอง",		null,			"🐞" }, // TH
			{ "甲蟲",		null,			"🐞" }, // TW
			{ "礼物",		"Gift",			"🎁" },
			{ "ของขวัญ",		null,			"🎁" }, //TH
			{ "禮物",		"gift",			"🎁" }, // TW + alternate key
			{ null,			"Watermelon",	"🍉" },
			{ "แตงโม",		null,			"🍉" }, // TH
//			{ "差劲",		"Pinky",		"" },
//			{ "ดีกัน",		null,			"" }, // TH
//			{ "差勁",		null,			"" }, // TW
			{ "爱你",		"RockOn",		"🤟" },
			{ "ฉันรักคุณ",	null,			"🤟" },
			{ "愛你",		null,			"🤟" },
			{ null,			"Love",			"😍" },
			{ null,			"NO",			"🙅" },
			{ "ไม่",			null,			"🙅" }, // TH
			{ "爱情",		"InLove",		"💕" },
			{ "รักกัน",		null,			"💕" }, // TH
			{ "熱戀",		"愛情",			"💕" }, // TW
			{ "飞吻",		"Blowkiss",		"😘" },
			{ "มีรัก",		null,			"😘" },
			{ "飛吻",		null,			"😘" },
			{ "闪电",		"Lightning",	"⚡" },
			{ "ฟ้าผ่า",		null,			"⚡" }, // TH
			{ "閃電",		null,			"⚡" }, // TW
			{ "刀",			"Dagger",		"🗡️" },		// Dup of "Cleaver"
			{ "ดาบ",		null,			"🗡️" }, // TH
			{ "足球",		"Soccer",		"⚽" },
			{ "棒球",		"Baseball",		"⚾" },
			{ "橄榄球",		"Football",		"🏈" },
			{ "ฟุตบอล",		null,			"🏈" }, // TH
			{ "橄欖球",		null,			"🏈" }, // TW
			{ "钱",			"Money",		"💰" },
			{ "錢",			null,			"💰" }, // TW
			{ "相机",		"Camera",		"📷" },
			{ "相機",		null,			"📷" }, // TW
			{ "干杯",		"Cheers",		"🍻" },
			{ "乾杯",		"乾杯",			"🍻" }, // TW
			{ "宝石",		"Gem",			"💎" },
			{ "鑽石",		null,			"💎" },
			{ "茶",			"Tea",			"🍵" },
			{ "药丸",		"Pill",			"💊" },
			{ "藥丸",		null,			"💊" }, // TW
			{ "庆祝",		"Party",		"🎉" },
			{ "慶祝",		null,			"🎉" }, // TW
			{ "火箭",		"Rocket ship",	"🚀" },
			{ "饥饿",		"Hungry", 		"😋" },
			{ "หิว",			null, 			"😋" }, // TH
			{ "饑餓",		null, 			"😋" }, // TW
			{ "酷",			"Ruthless", 	"😈" },
			{ "เจ๋ง",			null, 			"😈" }, // TH
			{ "吓",			"Uh Oh", 		"😠" },
			{ "嚇",			"Wrath", 		"😠" }, // Dup of above
			{ "ห๊า",			null, 			"😠" }, // TH
			{ "奋斗",		"Determined", 	"😣" },
			{ "疯了",		"Tormented", 	"😤" },
			{ "ท้อแท้",		null, 			"😤" }, // TH
			{ "瘋了",		null,		 	"😤" }, // TW
			{ "糗大了",		"Shame", 		"😳" },
			{ "อับอาย",		null, 			"😳" }, // TH
			{ "羞辱",		null,	 		"😳" }, // TW
			{ "磕头",		"Kotow",		"🙇" },
			{ "คำนับ",		null,			"🙇" }, // TH
			{ "磕頭",		null,			"🙇" }, // TW
			{ "回头",		"Lookback",		"🤚" },
			{ "เหลียวหลัง",	null,			"🤚" }, //TH
			{ "回頭",		null,			"🤚" },
//			{ "跳绳",		"Jump",			"" },
//			{ "跳繩",		"JumpRope",		"" }, // dup
//			{ "กระโดด",		null,			"" }, // TH
			{ "投降",		"Surrender",	"🏳️" },
			{ "ยอมแพ้",		null,			"🏳️" }, // TH
			{ "激动",		"Hooray",		"🙌" },
			{ "ไชโย",		null,			"🙌" }, // TH
			{ "激動",		null,			"🙌" },
//			{ "乱舞",		"HeyHey",		"" },
//			{ "亂舞",		null,			"" }, // TW
			{ "献吻",		"Smooch",		"😘" },
			{ "จูบ",			null,			"😘" }, // TH
//			{ "左太极",		"TaiJi L",		"" },
//			{ "左太極",		"TaiChi L",		"" }, //dup
//			{ "หญิงต่อสู้",		null,			"" }, // TH
//			{ "右太极",		"TaiJi R",		"" },
//			{ "右太極",		"TaiChi R",		"" }, //dup
//			{ "ชายต่อสู้",		null,			"" }, // TH
			{ null,			"Dramatic",		"🤚" }, // back of the hand
//			{ null,			"Meditate",		"" },
//			{ "เย้เย้",		null,			"" }, // TH
			{ "噴火",		null,			"🔥" },
	};
}
