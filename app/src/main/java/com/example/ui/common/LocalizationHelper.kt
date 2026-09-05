package com.example.ui.common

data class LanguageInfo(
    val code: String,
    val name: String,
    val nativeName: String,
    val region: String
)

object LocalizationHelper {
    val supportedLanguages = listOf(
        LanguageInfo("en", "English", "English (Default)", "Global / Pan-India"),
        LanguageInfo("hi", "Hindi", "हिंदी", "National"),
        LanguageInfo("as", "Assamese", "অসমীয়া", "Assam & Brahmaputra"),
        LanguageInfo("bn", "Bengali", "বাংলা", "West Bengal & Barak Valley"),
        LanguageInfo("ne", "Nepali", "नेपाली", "Sikkim & Darjeeling Hills"),
        LanguageInfo("bdo", "Bodo", "बड़ो", "Bodoland, Assam"),
        LanguageInfo("mni", "Manipuri", "মৈতৈলোন্ (Meitei)", "Manipur"),
        LanguageInfo("lus", "Mizo", "Mizo ṭawng", "Mizoram"),
        LanguageInfo("kha", "Khasi", "Ka Ktien Khasi", "Meghalaya"),
        LanguageInfo("gar", "Garo", "A·chik", "Garo Hills"),
        LanguageInfo("lep", "Lepcha", "Róng (Lepcha)", "Dzongu & Sikkim"),
        LanguageInfo("te", "Telugu", "తెలుగు", "South India"),
        LanguageInfo("ta", "Tamil", "தமிழ்", "South India"),
        LanguageInfo("mr", "Marathi", "मराठी", "Western India"),
        LanguageInfo("kn", "Kannada", "ಕನ್ನಡ", "South India"),
        LanguageInfo("ml", "Malayalam", "മലയാളം", "South India")
    )

    fun getLanguageName(code: String): String {
        return supportedLanguages.find { it.code == code }?.nativeName ?: "English"
    }

    // Localized string dictionary for key application labels
    fun getString(key: String, lang: String): String {
        return when (key) {
            "app_title" -> when (lang) {
                "hi" -> "टेराअलर्ट"
                "as" -> "টেৰাএলার্ট"
                "bn" -> "টেরাঅ্যালার্ট"
                "ne" -> "टेराअलर्ट"
                "bdo" -> "टेराअलार्ट"
                "mni" -> "তেনাএলৰ্ট"
                "lus" -> "TerraAlert"
                "te" -> "టెర్రాఅలర్ట్"
                "ta" -> "டெர்ராஅலர்ட்"
                "mr" -> "टेराअलर्ट"
                else -> "TerraAlert"
            }
            "app_subtitle" -> when (lang) {
                "hi" -> "पूर्वोत्तर भारत भूस्खलन पूर्व चेतावनी प्रणाली"
                "as" -> "উত্তৰ-পূব ভাৰত ভূমিস্খলন আগতীয়া সতৰ্কতা প্ৰণালী"
                "bn" -> "উত্তর-পূর্ব ভারত ভূমিধস আগাম সতর্কবার্তা ব্যবস্থা"
                "ne" -> "उत्तर-पूर्वी भारत पहिरो पूर्व चेतावनी प्रणाली"
                "bdo" -> "सानजा-सा भारत हास्रोबनाय सिगां सांग्रांथि राहा"
                "mni" -> "নোংপোক ভারত চিঙশেন পুংথোকপগী চেৎনারিবা মেকানিজম"
                "lus" -> "Hmarchhak India Leimin Hriattirna Hmanraw Rinawm"
                "te" -> "ఈశాన్య భారత కొండచరియల ముందస్తు హెచ్చరిక వ్యవస్థ"
                "ta" -> "வடகிழக்கு இந்திய நிலச்சரிவு முன்னெச்சரிக்கை அமைப்பு"
                "mr" -> "ईशान्य भारत भूस्खलन पूर्वसूचना प्रणाली"
                else -> "NE India Landslide Early Warning System"
            }
            "select_sector" -> when (lang) {
                "hi" -> "निगरानी क्षेत्र चुनें:"
                "as" -> "নিৰীক্ষণ খণ্ড নিৰ্বাচন কৰক:"
                "bn" -> "পর্যবেক্ষণ এলাকা নির্বাচন করুন:"
                "ne" -> "निगरानी क्षेत्र छान्नुहोस्:"
                "bdo" -> "नायबिजिरग्रा ओनसोल सायख':"
                "mni" -> "য়েংশিনবা মফম খনবিয়ু:"
                "lus" -> "Enzuihna Bial Thlang Rawh:"
                "te" -> "పర్యవేక్షణ ప్రాంతాన్ని ఎంచుకోండి:"
                "ta" -> "கண்காணிப்பு மண்டலத்தைத் தேர்ந்தெடுக்கவும்:"
                "mr" -> "निगरानी क्षेत्र निवडा:"
                else -> "Select Monitoring Sector:"
            }
            "current_safety_status" -> when (lang) {
                "hi" -> "वर्तमान सुरक्षा स्थिति"
                "as" -> "বৰ্তমান সুৰক্ষা স্থিতি"
                "bn" -> "বর্তমান নিরাপত্তা পরিস্থিতি"
                "ne" -> "वर्तमान सुरक्षा स्थिति"
                "bdo" -> "दासान्दि रैखाथि थासारि"
                "mni" -> "হৌজিক্কী কনবা ফীভম"
                "lus" -> "Tun dinhmun Himna Dinhmun"
                "te" -> "ప్రస్తుత భద్రతా స్థితి"
                "ta" -> "தற்போதைய பாதுகாப்பு நிலை"
                "mr" -> "सध्याची सुरक्षा स्थिती"
                else -> "CURRENT SAFETY STATUS"
            }
            "tab_monitor" -> when (lang) {
                "hi" -> "लाइव मॉनिटर"
                "as" -> "পোনপটীয়া নিৰীক্ষণ"
                "bn" -> "লাইভ মনিটর"
                "ne" -> "प्रत्यक्ष निगरानी"
                "bdo" -> "थोंजों नायबिजिर"
                "mni" -> "লাইভ মনিটর"
                "lus" -> "Enzui Mek"
                "te" -> "లైవ్ మానిటర్"
                "ta" -> "நேரலை கண்காணிப்பு"
                "mr" -> "थेट देखरेख"
                else -> "Live Monitor"
            }
            "tab_alerts" -> when (lang) {
                "hi" -> "सक्रिय अलर्ट"
                "as" -> "সক্ৰিয় সতৰ্কতা"
                "bn" -> "সক্রিয় সতর্কতা"
                "ne" -> "सक्रिय चेतावनी"
                "bdo" -> "जाफुंदों सांग्रांथि"
                "mni" -> "চেৎনারিবা চেকশিনৱা"
                "lus" -> "Hriattirna Awm Mek"
                "te" -> "యాక్టివ్ అలర్ట్‌లు"
                "ta" -> "செயலில் உள்ள எச்சரிக்கைகள்"
                "mr" -> "सक्रिय इशारे"
                else -> "Active Alerts"
            }
            "tab_safety" -> when (lang) {
                "hi" -> "सुरक्षा निर्देश"
                "as" -> "সুৰক্ষা নিৰ্দেশনা"
                "bn" -> "নিরাপত্তা নির্দেশিকা"
                "ne" -> "सुरक्षा मार्गदर्शक"
                "bdo" -> "रैखाथि लामानि लामा"
                "mni" -> "কনবগী লমজিংবা"
                "lus" -> "Himna Kawngkai"
                "te" -> "భద్రతా మార్గదర్శిని"
                "ta" -> "பாதுகாப்பு வழிகாட்டி"
                "mr" -> "सुरक्षा मार्गदर्शक"
                else -> "Safety Guide"
            }
            "tab_report" -> when (lang) {
                "hi" -> "खतरे की रिपोर्ट"
                "as" -> "বিপদৰ প্ৰতিবেদন"
                "bn" -> "বিপদ রিপোর্ট"
                "ne" -> "खतरा रिपोर्ट"
                "bdo" -> "खैफोद फोरमाय"
                "mni" -> "খুদোংথিবা রিপোর্ত"
                "lus" -> "Hlauhthawnna Thlen"
                "te" -> "ప్రమాదాన్ని నివేదించండి"
                "ta" -> "ஆபத்தை புகாரளிக்கவும்"
                "mr" -> "धोक्याची नोंद करा"
                else -> "Report Hazard"
            }
            "roads_heading" -> when (lang) {
                "hi" -> "राजमार्ग सुरक्षा स्थिति एवं सुरक्षित मार्ग"
                "as" -> "ৰাষ্ট্ৰীয় ঘাইপথৰ সুৰক্ষা আৰু বিকল্প পথ"
                "bn" -> "মহাসড়ক নিরাপত্তা ও বিকল্প নিরাপদ রুট"
                "ne" -> "राजमार्ग सुरक्षा स्थिति तथा वैकल्पिक बाटो"
                "bdo" -> "दालां-राजा लामा रैखाथि आरो सुबुं लामा"
                "mni" -> "হাইৱে কনবা ফীভম অমসুং সেফ রুত"
                "lus" -> "Kawngpui Himna & Kalphung Him Zawk"
                "te" -> "రహదారి భద్రత & సురక్షిత ప్రత్యామ్నాయ మార్గాలు"
                "ta" -> "நெடுஞ்சாலை பாதுகாப்பு & பாதுகாப்பான மாற்று வழி"
                "mr" -> "महामार्ग सुरक्षा स्थिती आणि पर्यायी मार्ग"
                else -> "Highway Safety & Safe Detour Routes"
            }
            "safe_road" -> when (lang) {
                "hi" -> "सुरक्षित (खुला)"
                "as" -> "সুৰক্ষিত (খোলা)"
                "bn" -> "নিরাপদ (খোলা)"
                "ne" -> "सुरक्षित (खुला)"
                "bdo" -> "रैखाथि (उदां)"
                "mni" -> "কনবা (হাংবা)"
                "lus" -> "Him (Inhawng)"
                "te" -> "సురక్షితం (ఓపెన్)"
                "ta" -> "பாதுகாப்பானது (திறந்தது)"
                "mr" -> "सुरक्षित (सुरू)"
                else -> "Safe (Open)"
            }
            "medium_road" -> when (lang) {
                "hi" -> "मध्यम (सावधानी)"
                "as" -> "মধ্যমীয়া (সাৱধান)"
                "bn" -> "মাঝারি (সতর্কতা)"
                "ne" -> "मध्यम (सतर्कता)"
                "bdo" -> "गेजेरारि (सांग्रां)"
                "mni" -> "মায়াম্না (চেকশিনবা)"
                "lus" -> "Fimkhur Ngai (Medium)"
                "te" -> "మధ్యస్థం (జాగ్రత్త)"
                "ta" -> "நடுத்தரம் (எச்சரிக்கை)"
                "mr" -> "मध्यम (सावधान)"
                else -> "Medium (Caution)"
            }
            "danger_road" -> when (lang) {
                "hi" -> "खतरा (अवरुद्ध)"
                "as" -> "বিপদজনক (অৱৰুদ্ধ)"
                "bn" -> "বিপদ (বন্ধ)"
                "ne" -> "खतरा (अवरुद्ध)"
                "bdo" -> "खैफोद (बन्द)"
                "mni" -> "খুদোংথিবা (থিংজিনবা)"
                "lus" -> "Hlauhawm (Ping)"
                "te" -> "ప్రమాదం (బ్లాక్ చేయబడింది)"
                "ta" -> "ஆபத்து (தடைபட்டது)"
                "mr" -> "धोकादायक (बंद)"
                else -> "Danger (Blocked)"
            }
            "weather_label" -> when (lang) {
                "hi" -> "मौसम एवं वर्षा"
                "as" -> "বতৰ আৰু বৰষুণ"
                "bn" -> "আবহাওয়া ও বৃষ্টিপাত"
                "ne" -> "मौसम तथा वर्षा"
                "bdo" -> "बारहावा आरो अखा"
                "mni" -> "নোং-নুংশিত অমসুং নোংচুবা"
                "lus" -> "Khawchin & Ruahsur"
                "te" -> "వాతావరణం & వర్షపాతం"
                "ta" -> "வானிலை & மழைப்பொழிவு"
                "mr" -> "हवामान आणि पाऊस"
                else -> "Weather & Rainfall"
            }
            "gis_map_title" -> when (lang) {
                "hi" -> "जीआईएस भूस्खलन जोखिम मानचित्र"
                "as" -> "জিআইএছ ভূমিস্খলন বিপদ মানচিত্ৰ"
                "bn" -> "জিআইএস ভূমিধস ঝুঁকি মানচিত্র"
                "ne" -> "जीआईएस पहिरो जोखिम नक्सा"
                "bdo" -> "GIS हास्रोबनाय खैफोद नक्सा"
                "mni" -> "GIS চিঙশেন খুদোংথিবা মেপ"
                "lus" -> "GIS Leimin Hlauhawm Map"
                "te" -> "జిఐఎస్ కొండచరియల ప్రమాద మ్యాప్"
                "ta" -> "ஜிஐஎஸ் நிலச்சரிவு இடர் வரைபடம்"
                "mr" -> "जीआयएस भूस्खलन धोका नकाशा"
                else -> "GIS Landslide Risk & Telemetry Map"
            }
            "map_size_standard" -> when (lang) {
                "hi" -> "मानक आकार"
                "as" -> "সাধাৰণ আকাৰ"
                "bn" -> "স্ট্যান্ডার্ড"
                "ne" -> "मानक आकार"
                else -> "Standard"
            }
            "map_size_large" -> when (lang) {
                "hi" -> "बड़ा मानचित्र"
                "as" -> "ডাঙৰ মেপ"
                "bn" -> "বৃহৎ মানচিত্র"
                "ne" -> "ठूलो नक्सा"
                else -> "Large Map"
            }
            "fullscreen" -> when (lang) {
                "hi" -> "पूर्ण स्क्रीन"
                "as" -> "সম্পূৰ্ণ স্ক্ৰীণ"
                "bn" -> "ফুলস্ক্রিন"
                "ne" -> "पूर्ण स्क्रिन"
                else -> "Fullscreen"
            }
            else -> key
        }
    }
}
