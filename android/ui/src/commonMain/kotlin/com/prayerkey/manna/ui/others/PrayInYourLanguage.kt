package com.prayerkey.manna.ui.others

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.platform.shareText
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans

/** A language a prayer can be prayed in. */
enum class PrayerTongue(val label: String, val native: String) {
    PIDGIN("Pidgin", "Naija Pidgin"),
    YORUBA("Yoruba", "Èdè Yorùbá"),
    IGBO("Igbo", "Asụsụ Igbo"),
    HAUSA("Hausa", "Harshen Hausa"),
    FRENCH("French", "Français"),
}

/** One prayer, in English and in each language. */
class TonguePrayer(val title: String, val english: String, val englishSource: String, val texts: Map<PrayerTongue, String>)

/*
 * The Lord's Prayer is given as it is prayed in each church: the English is
 * Matthew 6:9-13 in the King James Version, word for word; the French is the
 * shared Catholic and Protestant text of 2013. The other three prayers were
 * written for PrayerKey. Every non-English text is listed in
 * docs/translations-review.md for a native speaker to check before release.
 */
val TonguePrayers = listOf(
    TonguePrayer(
        "The Lord's Prayer",
        "Our Father which art in heaven, Hallowed be thy name. Thy kingdom come. Thy will be done in earth, as it is in heaven. Give us this day our daily bread. And forgive us our debts, as we forgive our debtors. And lead us not into temptation, but deliver us from evil: For thine is the kingdom, and the power, and the glory, for ever. Amen.",
        "Matthew 6:9-13, King James Version",
        mapOf(
            PrayerTongue.PIDGIN to "Our Papa wey dey heaven, make everybody respect your name. Make your kingdom come. Make wetin you want happen for earth, as e dey happen for heaven. Give us the food wey we go chop today. Forgive us our sin, as we sef dey forgive people wey do us bad. No let us fall enter temptation, but save us from the wicked one. Because the kingdom, the power and the glory na your own, forever and ever. Amen.",
            PrayerTongue.YORUBA to "Baba wa ti mbẹ li ọrun, ki a bọ̀wọ̀ fun orukọ rẹ. Ki ijọba rẹ de. Ifẹ tirẹ ni ki a ṣe, bi ti ọrun, bẹ̃ni li aiye. Fun wa li onjẹ ojọ wa loni. Dari gbese wa ji wa, bi awa ti ndari gbese ji awọn onigbese wa. Má si fà wa sinu idẹwò, ṣugbọn gbà wa lọwọ ibi. Nitori ijọba ni tirẹ, ati agbara, ati ogo, lai ati lailai. Amin.",
            PrayerTongue.IGBO to "Nna anyị nọ n'eluigwe, ka aha gị dị nsọ. Ka alaeze gị bịa. Ka e mee uche gị n'ụwa dịka e si eme ya n'eluigwe. Nye anyị taa nri anyị nke ụbọchị niile. Gbaghara anyị mmehie anyị, dịka anyị na-agbaghara ndị mehiere anyị. Edubakwala anyị n'ọnwụnwa, kama napụta anyị n'ihe ọjọọ. N'ihi na alaeze, na ike, na otuto bụ nke gị, ruo mgbe ebighị ebi. Amen.",
            PrayerTongue.HAUSA to "Ubanmu wanda ke cikin Sama, a tsarkake sunanka. Mulkinka ya zo. Nufinka ya zama a duniya kamar yadda yake a Sama. Ka ba mu abincinmu na yau. Ka gafarta mana laifofinmu, kamar yadda mu ma muke gafarta wa waɗanda suka yi mana laifi. Kada ka kai mu cikin jaraba, amma ka cece mu daga Mugun. Gama mulki, da iko, da ɗaukaka naka ne har abada abadin. Amin.",
            PrayerTongue.FRENCH to "Notre Père, qui es aux cieux, que ton nom soit sanctifié, que ton règne vienne, que ta volonté soit faite sur la terre comme au ciel. Donne-nous aujourd'hui notre pain de ce jour. Pardonne-nous nos offenses, comme nous pardonnons aussi à ceux qui nous ont offensés. Et ne nous laisse pas entrer en tentation, mais délivre-nous du Mal. Car c'est à toi qu'appartiennent le règne, la puissance et la gloire, pour les siècles des siècles. Amen.",
        ),
    ),
    TonguePrayer(
        "Morning prayer",
        "Lord, thank You for this new day. Go before me, guard my steps and my words, and let me carry Your peace into everything I do. In Jesus' name, Amen.",
        "Written for PrayerKey",
        mapOf(
            PrayerTongue.PIDGIN to "Lord, thank you for this new day. Waka for my front, guide my leg and my mouth, and make I carry your peace enter everything wey I go do today. For Jesus name, Amen.",
            PrayerTongue.YORUBA to "Oluwa, mo dupẹ lọwọ rẹ fun ọjọ tuntun yii. Ṣaaju mi lọ, ṣọ ìṣísẹ̀ mi ati ọ̀rọ̀ ẹnu mi, ki o si jẹ ki n gbe alaafia rẹ wọ inu ohun gbogbo ti mo ba ṣe. Ni orukọ Jesu, Amin.",
            PrayerTongue.IGBO to "Onyenwe anyị, daalụ maka ụbọchị ọhụrụ a. Bute m ụzọ, chekwaa nzọụkwụ m na okwu m, ma mee ka m buru udo gị baa n'ihe niile m ga-eme. N'aha Jizọs, Amen.",
            PrayerTongue.HAUSA to "Ya Ubangiji, na gode maka saboda wannan sabuwar rana. Ka yi mini jagora, ka kiyaye matakaina da maganata, ka sa in ɗauki salamarka cikin duk abin da zan yi. Cikin sunan Yesu, Amin.",
            PrayerTongue.FRENCH to "Seigneur, merci pour ce jour nouveau. Va devant moi, garde mes pas et mes paroles, et fais que je porte ta paix dans tout ce que je ferai. Au nom de Jésus, Amen.",
        ),
    ),
    TonguePrayer(
        "Night prayer",
        "Father, into Your hands I give this day, what went well and what did not. Forgive me where I failed, watch over my home tonight, and give me quiet sleep. In Jesus' name, Amen.",
        "Written for PrayerKey",
        mapOf(
            PrayerTongue.PIDGIN to "Papa God, I put today for your hand, the good and the bad. Forgive me where I fall hand, watch over my house tonight, and give me better sleep. For Jesus name, Amen.",
            PrayerTongue.YORUBA to "Baba, mo fi ọjọ oni le ọ lọwọ, eyi ti o dara ati eyi ti ko dara. Dariji mi nibi ti mo ti kuna, ṣọ ile mi ni alẹ yii, ki o si fun mi ni oorun alaafia. Ni orukọ Jesu, Amin.",
            PrayerTongue.IGBO to "Nna, ana m enyefe ụbọchị a n'aka gị, ihe dị mma na ihe na-adịghị mma. Gbaghara m ebe m dara, chebe ụlọ m n'abalị a, ma nye m ụra udo. N'aha Jizọs, Amen.",
            PrayerTongue.HAUSA to "Ya Uba, na danƙa maka wannan rana, abin da ya yi kyau da abin da bai yi ba. Ka gafarta mini inda na kasa, ka tsare gidana a daren nan, ka ba ni barci mai salama. Cikin sunan Yesu, Amin.",
            PrayerTongue.FRENCH to "Père, je remets entre tes mains cette journée, ce qui a été bien et ce qui ne l'a pas été. Pardonne-moi là où j'ai failli, veille sur ma maison cette nuit, et donne-moi un sommeil paisible. Au nom de Jésus, Amen.",
        ),
    ),
    TonguePrayer(
        "Grace before food",
        "Lord, bless this food and the hands that made it. Give bread to those who have none, and make us thankful. In Jesus' name, Amen.",
        "Written for PrayerKey",
        mapOf(
            PrayerTongue.PIDGIN to "Lord, bless this food and the hand wey cook am. Give food to people wey no get, and make we dey thankful. For Jesus name, Amen.",
            PrayerTongue.YORUBA to "Oluwa, bukun onjẹ yii ati ọwọ ti o se e. Fun awọn ti ko ni onjẹ ni onjẹ, ki o si jẹ ki a maa dupẹ. Ni orukọ Jesu, Amin.",
            PrayerTongue.IGBO to "Onyenwe anyị, gọzie nri a na aka ndị siri ya. Nye ndị na-enweghị nri, ma mee ka anyị na-ekele gị. N'aha Jizọs, Amen.",
            PrayerTongue.HAUSA to "Ya Ubangiji, ka albarkaci wannan abinci da hannayen da suka dafa shi. Ka ba waɗanda ba su da abinci, ka sa mu zama masu godiya. Cikin sunan Yesu, Amin.",
            PrayerTongue.FRENCH to "Seigneur, bénis ce repas et les mains qui l'ont préparé. Donne du pain à ceux qui n'en ont pas, et rends-nous reconnaissants. Au nom de Jésus, Amen.",
        ),
    ),
)

/**
 * Pray in your language: four prayers every Christian household knows,
 * in the languages people actually pray in at home, with the English beside
 * them. The chosen language is remembered.
 */
@Composable
fun PrayInYourLanguageScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val store = remember { context.keyValues("pray_language") }
    var tongue by remember {
        mutableStateOf(PrayerTongue.entries.firstOrNull { it.name == store.getString("tongue", "") } ?: PrayerTongue.PIDGIN)
    }
    var showEnglish by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2C0A5C), Color(0xFF12002A))))) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())) {
            Box(
                Modifier.padding(start = 20.dp, top = 12.dp).size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f)).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp)) }
            Text(
                "Pray in your language", Modifier.padding(start = 20.dp, top = 14.dp, end = 20.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp, color = Color.White,
            )
            Text(
                "O praise the LORD, all ye nations: praise him, all ye people. Psalm 117:1",
                Modifier.padding(start = 20.dp, top = 4.dp), fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 16.sp, color = Gold,
            )
            Row(
                Modifier.padding(top = 20.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PrayerTongue.entries.forEach { t ->
                    val on = t == tongue
                    Column(
                        Modifier.clip(RoundedCornerShape(18.dp)).background(if (on) Gold else Color.White.copy(alpha = .09f))
                            .clickable { tongue = t; store.edit().putString("tongue", t.name).apply() }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text(t.label, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (on) DeepViolet else Color.White)
                        Text(t.native, fontFamily = UtilitySans, fontSize = 11.sp, color = if (on) DeepViolet.copy(alpha = .7f) else Color.White.copy(alpha = .55f))
                    }
                }
            }
            Text(
                if (showEnglish) "Hide the English" else "Show the English beside it",
                Modifier.padding(start = 20.dp, top = 14.dp).clickable { showEnglish = !showEnglish }.padding(vertical = 4.dp),
                fontFamily = UtilitySans, fontSize = 13.sp, color = Gold.copy(alpha = .85f),
            )
            TonguePrayers.forEach { p ->
                val text = p.texts[tongue].orEmpty()
                Column(
                    Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp))
                        .background(Color.White).padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(p.title.uppercase(), Modifier.weight(1f), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.4.sp, color = Violet)
                        Icon(
                            Icons.Outlined.Share, "Share this prayer", tint = Violet,
                            modifier = Modifier.size(34.dp).clip(CircleShape)
                                .clickable { shareText(context, "${p.title}\n\n$text\n\nPrayed with PrayerKey: https://www.prayerkey.com", "Share") }
                                .padding(7.dp),
                        )
                    }
                    Text(text, Modifier.padding(top = 8.dp), fontFamily = DisplaySerif, fontSize = 20.sp, lineHeight = 30.sp, color = Color(0xFF1D1D1F))
                    if (showEnglish) {
                        Text(
                            p.english, Modifier.padding(top = 14.dp),
                            fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF6E6E73),
                        )
                        Text(p.englishSource, Modifier.padding(top = 4.dp), fontFamily = UtilitySans, fontSize = 11.sp, color = Color(0xFF9A9AA0))
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
