package com.remote.app.i18n

import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("system", "System Default"),
    EN_US("en-US", "English (US)"),
    EN_UK("en-GB", "English (UK)"),
    EN_AU("en-AU", "English (Australia)"),
    ES_ES("es-ES", "Español (España)"),
    ES_419("es-419", "Español (Latinoamérica)"),
    ES_US("es-US", "Español (US)"),
    FR("fr", "Français"),
    PT_PT("pt-PT", "Português (Portugal)"),
    PT_BR("pt-BR", "Português (Brasil)"),
    ZH_CN("zh-CN", "中文 (Mandarin / Simplified)"),
    ZH_HK("zh-HK", "廣東話 (Cantonese / HK)"),
    ZH_TW("zh-TW", "中文 (Mandarin / Traditional)"),
    VI("vi", "Tiếng Việt"),
    JA("ja", "日本語"),
    RU("ru", "Русский"),
    DE("de", "Deutsch"),
    PIRATE("en-PIRATE", "Pirate English \uD83C\uDFF4\u200D\u2620\uFE0F"),
    KLINGON("tlh", "Klingon \uD83D\uDD96"),
    DOTHRAKI("art", "Dothraki \uD83D\uDC0E"),
    ELVISH("sjn", "Elvish (Sindarin) \uD83E\uDDDD"),
    MORDOR("sbs", "Black Speech (Mordor) \uD83C\uDF0B"),
    LATIN("la", "Latina"),
    ESPERANTO("eo", "Esperanto")
}

data class AppStrings(
    val scanForTvs: String,
    val scanning: String,
    val noTvsFound: String,
    val error: String,
    val online: String,
    val offline: String,
    val never: String,
    val secsAgo: String,
    val minsAgo: (Long) -> String,
    val hoursAgo: (Long) -> String,
    val daysAgo: (Long) -> String,
    val disconnect: String,
    val power: String,
    val enterPinPrompt: String,
    val pair: String,
    val settings: String,
    val about: String,
    val developer: String,
    val website: String,
    val language: String,
    val systemDefault: String,
    val proVersion: String,
    val buyProDesc: String
)

val enUsStrings = AppStrings(
    scanForTvs = "Scan for TVs", scanning = "Scanning...", noTvsFound = "No TVs found. Press Scan to find devices on your network.",
    error = "Error", online = "Online", offline = "Offline", never = "Never", secsAgo = "Seconds ago",
    minsAgo = { "${it}m ago" }, hoursAgo = { "${it}h ago" }, daysAgo = { "${it}d ago" },
    disconnect = "Disconnect", power = "Power", enterPinPrompt = "Please enter the PIN displayed on your TV:",
    pair = "Pair", settings = "Settings", about = "About", developer = "Developer", website = "Website",
    language = "Language", systemDefault = "System Default",
    proVersion = "Pro Version", buyProDesc = "Remove ads forever with a one-time purchase or monthly sub."
)

val enUkStrings = enUsStrings.copy(scanForTvs = "Search for TVs", noTvsFound = "No TVs found. Press Search to locate devices on your network.")
val enAuStrings = enUsStrings.copy(scanForTvs = "Find Mates' TVs", noTvsFound = "No TVs found, mate. Press Find to spot devos.")
val pirateStrings = enUsStrings.copy(
    scanForTvs = "Scour fer TVs", scanning = "Scourin'...", noTvsFound = "No scallywag TVs spotted. Scour again ye scurvy dog.",
    error = "Mutiny", online = "Sailin'", offline = "Sunken", never = "Never", secsAgo = "Just now",
    minsAgo = { "${it}m past" }, hoursAgo = { "${it} bells ago" }, daysAgo = { "${it} suns ago" },
    disconnect = "Abandon Ship", power = "Snuff Out", enterPinPrompt = "Carve the secret code displayed yonder:",
    pair = "Board", settings = "Captain's Log", about = "Tale", developer = "Shipwright", website = "Sails",
    language = "Tongue", systemDefault = "Fleet Standard", proVersion = "Captain's Cut", buyProDesc = "Throw the ads o'erboard fer good!"
)

val esEsStrings = AppStrings(
    scanForTvs = "Buscar TVs", scanning = "Buscando...", noTvsFound = "No se encontraron TVs. Presiona Buscar para localizar dispositivos en tu red.",
    error = "Error", online = "En línea", offline = "Desconectado", never = "Nunca", secsAgo = "Hace unos segs",
    minsAgo = { "Hace ${it}m" }, hoursAgo = { "Hace ${it}h" }, daysAgo = { "Hace ${it}d" },
    disconnect = "Desconectar", power = "Apagar", enterPinPrompt = "Por favor, introduce el PIN mostrado en tu TV:",
    pair = "Emparejar", settings = "Ajustes", about = "Acerca de", developer = "Desarrollador", website = "Sitio Web",
    language = "Idioma", systemDefault = "Por defecto del sistema",
    proVersion = "Versión Pro", buyProDesc = "Elimina la publicidad para siempre con un pago único o mensual."
)
val es419Strings = esEsStrings.copy(scanForTvs = "Escanear TVs", noTvsFound = "No se encontraron TVs. Presiona Escanear para hallar dispositivos en la red.", settings = "Configuración")
val esUsStrings = es419Strings.copy(enterPinPrompt = "Por favor ingrese el PIN de la TV:")

val frStrings = AppStrings(
    scanForTvs = "Scanner les TV", scanning = "Recherche...", noTvsFound = "Aucune TV trouvée. Appuyez sur Scanner.",
    error = "Erreur", online = "En ligne", offline = "Hors ligne", never = "Jamais", secsAgo = "Il y a qqs secs",
    minsAgo = { "Il y a ${it}m" }, hoursAgo = { "Il y a ${it}h" }, daysAgo = { "Il y a ${it}j" },
    disconnect = "Déconnecter", power = "Éteindre", enterPinPrompt = "Veuillez entrer le code PIN affiché sur la TV:",
    pair = "Associer", settings = "Paramètres", about = "À propos", developer = "Développeur", website = "Site Web",
    language = "Langue", systemDefault = "Par défaut", proVersion = "Version Pro", buyProDesc = "Supprimez les publicités pour toujours."
)

val ptPtStrings = AppStrings(
    scanForTvs = "Procurar TVs", scanning = "A procurar...", noTvsFound = "Nenhuma TV encontrada. Pressione Procurar.",
    error = "Erro", online = "Online", offline = "Offline", never = "Nunca", secsAgo = "Há segundos",
    minsAgo = { "Há ${it}m" }, hoursAgo = { "Há ${it}h" }, daysAgo = { "Há ${it}d" },
    disconnect = "Desconectar", power = "Desligar", enterPinPrompt = "Por favor, insira o PIN exibido na TV:",
    pair = "Emparelhar", settings = "Definições", about = "Sobre", developer = "Programador", website = "Website",
    language = "Idioma", systemDefault = "Predefinição do sistema", proVersion = "Versão Pro", buyProDesc = "Remova anúncios para sempre."
)
val ptBrStrings = ptPtStrings.copy(scanForTvs = "Escanear TVs", scanning = "Escaneando...", noTvsFound = "Nenhuma TV encontrada. Pressione Escanear.", settings = "Configurações", developer = "Desenvolvedor")

val zhCnStrings = AppStrings(
    scanForTvs = "扫描电视", scanning = "扫描中...", noTvsFound = "未找到电视。请按扫描查找网络上的设备。",
    error = "错误", online = "在线", offline = "离线", never = "从未", secsAgo = "几秒前",
    minsAgo = { "${it}分钟前" }, hoursAgo = { "${it}小时前" }, daysAgo = { "${it}天前" },
    disconnect = "断开连接", power = "电源", enterPinPrompt = "请输入电视上显示的 PIN 码：",
    pair = "配对", settings = "设置", about = "关于", developer = "开发者", website = "网站",
    language = "语言", systemDefault = "系统默认", proVersion = "专业版", buyProDesc = "一次性购买彻底移除广告。"
)
val zhTwStrings = zhCnStrings.copy(
    scanForTvs = "掃描電視", scanning = "掃描中...", noTvsFound = "未找到電視。請按掃描查找網路上的設備。",
    error = "錯誤", online = "在線", offline = "離線", never = "從未", secsAgo = "幾秒前",
    minsAgo = { "${it}分鐘前" }, hoursAgo = { "${it}小時前" }, daysAgo = { "${it}天前" },
    disconnect = "斷開連接", power = "電源", enterPinPrompt = "請輸入電視上顯示的 PIN 碼：",
    pair = "配對", settings = "設置", about = "關於", developer = "開發者", website = "網站",
    language = "語言", systemDefault = "系統默認", proVersion = "專業版", buyProDesc = "一次性購買徹底移除廣告。"
)
val zhHkStrings = zhTwStrings.copy(scanForTvs = "搵電視", scanning = "搵緊...", noTvsFound = "搵唔到電視，請撳搵電視掣。", disconnect = "中斷連線")

val viStrings = AppStrings(
    scanForTvs = "Quét TV", scanning = "Đang quét...", noTvsFound = "Không tìm thấy TV nào. Vui lòng quét lại.",
    error = "Lỗi", online = "Trực tuyến", offline = "Ngoại tuyến", never = "Chưa bao giờ", secsAgo = "Vài giây trước",
    minsAgo = { "${it}p trước" }, hoursAgo = { "${it}g trước" }, daysAgo = { "${it}n trước" },
    disconnect = "Ngắt kết nối", power = "Nguồn", enterPinPrompt = "Vui lòng nhập mã PIN trên TV:",
    pair = "Ghép nối", settings = "Cài đặt", about = "Giới thiệu", developer = "Nhà phát triển", website = "Trang web",
    language = "Ngôn ngữ", systemDefault = "Mặc định hệ thống", proVersion = "Bản Pro", buyProDesc = "Xóa quảng cáo trọn đời."
)

val jaStrings = AppStrings(
    scanForTvs = "TVをスキャン", scanning = "スキャン中...", noTvsFound = "TVが見つかりません。スキャンを押してください。",
    error = "エラー", online = "オンライン", offline = "オフライン", never = "なし", secsAgo = "数秒前",
    minsAgo = { "${it}分前" }, hoursAgo = { "${it}時間前" }, daysAgo = { "${it}日前" },
    disconnect = "切断", power = "電源", enterPinPrompt = "TVに表示されたPINを入力してください：",
    pair = "ペアリング", settings = "設定", about = "情報", developer = "開発者", website = "ウェブサイト",
    language = "言語", systemDefault = "システムデフォルト", proVersion = "プロ版", buyProDesc = "広告を永久に非表示にします。"
)

val ruStrings = AppStrings(
    scanForTvs = "Поиск ТВ", scanning = "Поиск...", noTvsFound = "ТВ не найдены. Нажмите Поиск.",
    error = "Ошибка", online = "В сети", offline = "Не в сети", never = "Никогда", secsAgo = "Только что",
    minsAgo = { "${it}м назад" }, hoursAgo = { "${it}ч назад" }, daysAgo = { "${it}д назад" },
    disconnect = "Отключить", power = "Питание", enterPinPrompt = "Введите PIN, отображаемый на ТВ:",
    pair = "Сопряжение", settings = "Настройки", about = "О приложении", developer = "Разработчик", website = "Сайт",
    language = "Язык", systemDefault = "Системный", proVersion = "Pro-версия", buyProDesc = "Удалить рекламу навсегда."
)

val deStrings = AppStrings(
    scanForTvs = "TVs suchen", scanning = "Suchen...", noTvsFound = "Keine TVs gefunden. Drücke Suchen.",
    error = "Fehler", online = "Online", offline = "Offline", never = "Nie", secsAgo = "Gerade eben",
    minsAgo = { "Vor ${it}m" }, hoursAgo = { "Vor ${it}h" }, daysAgo = { "Vor ${it}t" },
    disconnect = "Trennen", power = "Ausschalten", enterPinPrompt = "Bitte PIN vom TV eingeben:",
    pair = "Koppeln", settings = "Einstellungen", about = "Über", developer = "Entwickler", website = "Webseite",
    language = "Sprache", systemDefault = "Systemstandard", proVersion = "Pro-Version", buyProDesc = "Werbung für immer entfernen."
)

val klingonStrings = enUsStrings.copy(
    scanForTvs = "bej tv", scanning = "bej...", noTvsFound = "tv pagh. bej.", online = "yIn", offline = "Hegh",
    never = "paghlogh", disconnect = "mev", power = "Qor", pair = "tay", settings = "choH",
    language = "Hol", systemDefault = "pat Hol"
)

val dothrakiStrings = enUsStrings.copy(
    scanForTvs = "Tih TV", scanning = "Tih...", noTvsFound = "TV vos. Tih.", online = "Qoy", offline = "Driv",
    never = "Vosma", disconnect = "Zireyes", power = "Zhor", pair = "Fich", settings = "Vezhv",
    language = "Lekh", systemDefault = "Sistih Lekh"
)

val elvishStrings = enUsStrings.copy(
    scanForTvs = "Cenë TVs", scanning = "Cenë...", noTvsFound = "Umin hirë TVs. Cenë.", online = "Cuina", offline = "Firin",
    never = "Ullumë", disconnect = "Hecë", power = "Gwî", pair = "Vesta", settings = "Natyë",
    language = "Lambë", systemDefault = "Marta Lambë"
)

val mordorStrings = enUsStrings.copy(
    scanForTvs = "Gimb TVs", scanning = "Gimbatul...", noTvsFound = "Snaga TVs. Gimb.", online = "Gûl", offline = "Morgul",
    never = "Bûb", disconnect = "Krim", power = "Ghash", pair = "Thrak", settings = "Dur",
    language = "Durbatuluk", systemDefault = "Lugbûrz Durbatuluk"
)

val latinStrings = AppStrings(
    scanForTvs = "Quaere TVs", scanning = "Quaerens...", noTvsFound = "Nullae TVs inventae. Preme Quaere.",
    error = "Error", online = "Connexus", offline = "Disiunctus", never = "Nunquam", secsAgo = "Nunc",
    minsAgo = { "Ante ${it}m" }, hoursAgo = { "Ante ${it}h" }, daysAgo = { "Ante ${it}d" },
    disconnect = "Disiunge", power = "Verte", enterPinPrompt = "Scribe PIN exhibitum:",
    pair = "Coniunge", settings = "Optiones", about = "De", developer = "Fictor", website = "Rete",
    language = "Lingua", systemDefault = "Defalta", proVersion = "Versio Pro", buyProDesc = "Remove praeconia in perpetuum."
)

val esperantoStrings = AppStrings(
    scanForTvs = "Skani TV-ojn", scanning = "Skanante...", noTvsFound = "Neniuj TV-oj trovitaj. Premu Skani.",
    error = "Eraro", online = "Enrete", offline = "Eksterrete", never = "Neniam", secsAgo = "Ĵus nun",
    minsAgo = { "Antaŭ ${it}m" }, hoursAgo = { "Antaŭ ${it}h" }, daysAgo = { "Antaŭ ${it}t" },
    disconnect = "Malkonekti", power = "Malŝalti", enterPinPrompt = "Bonvolu enigi la PIN-on:",
    pair = "Parigi", settings = "Agordoj", about = "Pri", developer = "Programisto", website = "Retejo",
    language = "Lingvo", systemDefault = "Sistema Defaŭlto", proVersion = "Pro-Versio", buyProDesc = "Forigu reklamojn por ĉiam."
)

fun getAppStrings(appLanguage: AppLanguage): AppStrings {
    if (appLanguage == AppLanguage.SYSTEM) {
        val sysLang = Locale.getDefault().language
        val sysCountry = Locale.getDefault().country
        return when (sysLang) {
            "es" -> if (sysCountry == "ES") esEsStrings else if (sysCountry == "US") esUsStrings else es419Strings
            "fr" -> frStrings
            "pt" -> if (sysCountry == "BR") ptBrStrings else ptPtStrings
            "zh" -> if (sysCountry == "TW") zhTwStrings else if (sysCountry == "HK") zhHkStrings else zhCnStrings
            "vi" -> viStrings
            "ja" -> jaStrings
            "ru" -> ruStrings
            "de" -> deStrings
            "en" -> if (sysCountry == "GB") enUkStrings else if (sysCountry == "AU") enAuStrings else enUsStrings
            else -> enUsStrings
        }
    }
    return when (appLanguage) {
        AppLanguage.EN_US -> enUsStrings
        AppLanguage.EN_UK -> enUkStrings
        AppLanguage.EN_AU -> enAuStrings
        AppLanguage.ES_ES -> esEsStrings
        AppLanguage.ES_419 -> es419Strings
        AppLanguage.ES_US -> esUsStrings
        AppLanguage.FR -> frStrings
        AppLanguage.PT_PT -> ptPtStrings
        AppLanguage.PT_BR -> ptBrStrings
        AppLanguage.ZH_CN -> zhCnStrings
        AppLanguage.ZH_HK -> zhHkStrings
        AppLanguage.ZH_TW -> zhTwStrings
        AppLanguage.VI -> viStrings
        AppLanguage.JA -> jaStrings
        AppLanguage.RU -> ruStrings
        AppLanguage.DE -> deStrings
        AppLanguage.PIRATE -> pirateStrings
        AppLanguage.KLINGON -> klingonStrings
        AppLanguage.DOTHRAKI -> dothrakiStrings
        AppLanguage.ELVISH -> elvishStrings
        AppLanguage.MORDOR -> mordorStrings
        AppLanguage.LATIN -> latinStrings
        AppLanguage.ESPERANTO -> esperantoStrings
        else -> enUsStrings
    }
}
