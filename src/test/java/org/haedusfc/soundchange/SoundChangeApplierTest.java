package org.haedusfc.soundchange;

import org.haedusfc.datatypes.phonetic.Sequence;
import org.haedusfc.soundchange.exceptions.RuleFormatException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 9/19/13
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class SoundChangeApplierTest {
	private static final transient Logger LOGGER = LoggerFactory.getLogger(SoundChangeApplierTest.class);
	@Test
	public void simpleRuleTest01() throws RuleFormatException
	{
		String[] commands = {
				"a > e",
				"d > t / _#"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("abad", "abada", "ad", "ado");
		List<String> expected = toList("ebet", "ebede", "et", "edo");

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected), received);
	}

	@Test
	public void simpleRuleTest02() throws RuleFormatException
	{
		String[] commands = {
				"ḱʰ ḱ ǵ > cʰ c ɟ",
				"cʰs cs ɟs > ks ks ks",
				"s > 0 / {cʰ  c  ɟ}_",
				"tk tʰkʰ ct ɟt ck  > ks ks ɕt ɕt ɕk",
				"tc dc tcʰ tʰcʰ > cc"
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("ruḱso", "tkeh",  "oḱto", "artḱos");
		List<String> expected = toList("rukso",  "kseh", "oɕto", "arccos");

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected), received);
	}

	@Test
	public void simpleRuleTest03() throws RuleFormatException
	{
		String[] commands = {
				"- > 0",
				"h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ",
				"hₓ hₐ > ʔ ʕ",
				"b  d  ǵ  g  gʷ  > bʰ dʰ ǵʰ gʰ gʷʰ / _{ x ɣ }",
				"p  t  ḱ  k  kʷ  > pʰ tʰ ḱʰ kʰ kʷʰ / _{x ɣ}",
				"bʰ dʰ ǵʰ gʰ gʷʰ > pʰ tʰ ḱʰ kʰ kʷʰ",
				"ḱ ḱʰ ǵ ǵʰ > c cʰ ɟ ɟʰ"};

		List<String> words = toList(
				"h₂oḱ-ri-",		"bʰaḱehₐ-",
				"dʰh₁ilehₐ-",	"ǵenh₁trihₐ-",
				"h₂rǵ-i-ḱuon-",	"h₂wedh₂-",
				"dʰǵʰuhₓ",		"dʰh₁ilehₐ-");

		List<String> expected = toList(
				"xocri",		"pʰaceʕ",
				"tʰʔileʕ",		"ɟenʔtriʕ",
				"xrɟicuon",		"xwetʰx",
				"tʰcʰuʔ",		"tʰʔileʕ");

		SoundChangeApplier sca = new SoundChangeApplier(commands);
		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected), received);
	}

	@Test
	public void simpleRuleTest04() throws RuleFormatException
	{
		String[] commands = {
				"e é ē ê > a á ā â / {x ʕ}_",
				"e é ē ê > a á ā â / _{x ʕ}",
				"e é ē ê > o ó ō ô / ɣ_",
				"e é ē ê > o ó ō ô / _ɣ",
				"ɣ ʕ > x ʔ",
				"x ʔ > 0 / _{x ʔ}",
				"x ʔ > 0 / _{i y í}",
				"ix iʔ ux uʔ > ī ī ū ū"};

		List<String> words = toList(
				"xocri",	"pʰaceʕ",
				"tʰʔileʕ",	"ɟenʔtriʕ",
				"xrɟicuon",	"xwetʰx",
				"tʰcʰuʔ",	"tʰʔileʕ");

		List<String> expected = toList(
				"xocri",	"pʰacaʔ",
				"tʰilaʔ",	"ɟenʔtrī",
				"xrɟicuon",	"xwetʰx",
				"tʰcʰū",	"tʰilaʔ");

		SoundChangeApplier sca = new SoundChangeApplier(commands);
		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected), received);
	}

	@Test
	public void reassignmentTest01() throws RuleFormatException {
		String[] commands = {
				"% Comment",
				"C = p t k pʰ tʰ kʰ n m r l",
				"e o > i u / _C",

				"C = p t k",
				"i u > a a / _C",
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("otetʰ");
		List<String> expected = toList("atitʰ");

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected),received);
	}

	@Test
	public void ruleTest01() throws RuleFormatException
	{
		String[] commands = {
				"% Comment",
				"C = p t k pʰ tʰ kʰ n m r l",
				"e o > i u / #_C",
		};

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList("epet", "epete", "et", "eto", "om", "elon", "tet");
		List<String> expected = toList("ipet", "ipete", "it", "ito", "um", "ilon", "tet");

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected),received);
	}

	@Test
	public void ruleTest02() throws RuleFormatException
	{
		String commands =
				"% Comment\n" +
				"C  = p t k pʰ tʰ kʰ n m r l\n"+
				"CH = pʰ tʰ kʰ\n"+
				"G  = b d g\n" +
				"R = r l\n"                  +
				"V = a e i o u ā ē ī ō ū\n" +

				"CH > G / _R?VV?C*CH\n";

		SoundChangeApplier sca = new SoundChangeApplier(commands);

		List<String> words    = toList(
				"pʰapʰa"
									  );
		List<String> expected = toList(
				"bapʰa"
									  );

		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected),received);
	}

	@Test
	public void testExpansion01() throws RuleFormatException
	{
		String commands = "" +
				"[E] = e ē\n" +
				"[A] = a ā\n" +
				"[O] = o ō\n" +
				"[I] = i ī\n" +
				"[U] = u ū\n" +

				"@X = x ʔ\n" +
				"N = m n\n" +
				"R = r l\n" +
				"A = N R\n" +
				"W = j w\n" +

				"@Q  = kʷʰ kʷ gʷ\n" +
				"@K  = kʰ  k  g\n" +
				"@KY = cʰ  c  ɟ\n" +
				"@P  = pʰ  p  b\n" +
				"@T  = tʰ  t  d\n" +
				"[PLOSIVE]   = @P @T @KY @K @Q\n" +
				"[OBSTRUENT] = [PLOSIVE] s\n" +
				"C   = [OBSTRUENT] A W\n" +
				"@VS = a e i o u ə\n" +
				"@VL = ā ē ī ō ū ə̄\n" +
				"@AE = [A] [E] [O]\n" +
				"V   = @VS @VL \n" +
				"@IU = i u";

		SoundChangeApplier sca = new SoundChangeApplier(commands);
		sca.processLexicon(new ArrayList<String>());

		VariableStore vs = sca.getVariables();

		LOGGER.info(vs.get("[PLOSIVE]").toString());

		testExpansion(vs, "V", "a e i o u ə ā ē ī ō ū ə̄");
		testExpansion(vs, "C", "pʰ p b tʰ t d cʰ c ɟ kʰ k g kʷʰ kʷ gʷ s m n r l j w");
	}

	private void testExpansion(VariableStore vs, String key, String terminals) {
		assertEquals(toSequences(terminals),vs.get(key));
	}


	@Test
	public void ruleTestUltra() throws RuleFormatException {
		String commands = "" +
				"y w > i u\n" +

				"C P T K V R > k p t k a r\n" +

				"ā́  ḗ  ī́  ṓ  ū́  > â ê î ô û\n" +
				"á  é  í  ó  ú  > á é í ó ú\n" +

				"[E] = e ē é ê\n" +
				"[A] = a ā á â\n" +
				"[O] = o ō ó ô\n" +
				"[I] = i ī í î\n" +
				"[U] = u ū ú û\n" +

				"X = x ʔ\n" +
				"N = m n\n" +
				"R = r l\n" +
				"A = N R\n" +
				"W = y w\n" +

				"@Q  = kʷʰ kʷ gʷ\n" +
				"@K  = kʰ  k  g\n" +
				"@KY = cʰ  c  ɟ\n" +
				"@P  = pʰ  p  b\n" +
				"@T  = tʰ  t  d\n" +
				"[PLOSIVE]   = @P @T @KY @K @Q\n" +
				"[OBSTRUENT] = [PLOSIVE] s\n" +
				"C   = [OBSTRUENT] A W\n" +
				"@VS = a e i o u ə á é í ó ú\n" +
				"@VL = ā ē ī ō ū ə̄  â ê î ô û\n" +
				"@AE = [A] [E] [O]\n" +
				"V   = @VS @VL \n" +
				"@IU = i u í ú\n" +

				"ʜ > ʔ\n" +

				"% Szemerenyi's law\n" +
				"@VS > @VL / _m-m#\n" +
				"a e o á é ó > ā ē ō â ê ô / _{s n?t r l m}-s#\n" +
				"-m > 0 / m_#\n" +
				"-s > 0 / {s t r l m}_#\n" +

				"% For correct handling of negation prefix\n" +
				"n- > n̩ / #_\n" +

				"ā́  > â\n" +

				"ee eo oe ea ae > ē ō ō ā ā\n" +
				"ée eé éo eó óe oé > ê ê ô ô ô ô\n" +
				"éa eá áe aé > â â â â\n" +

				"- > 0\n" +

				"%%%% PHASE 01 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +
				"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +

				"h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ\n" +
				"hₓ hₐ > ʔ ʕ\n" +

				"b  d  ǵ  g  gʷ  > bʰ dʰ ǵʰ gʰ gʷʰ / _{x ɣ}\n" +
				"p  t  ḱ  k  kʷ  > pʰ tʰ ḱʰ kʰ kʷʰ / _{x ɣ}\n" +
				"bʰ dʰ ǵʰ gʰ gʷʰ > pʰ tʰ ḱʰ kʰ kʷʰ\n" +

				"ḱʰ ḱ ǵ > cʰ c ɟ\n" +

				"[E] > [A] / {x ʕ}_\n" +
				"[E] > [A] / _{x ʕ}\n" +
				"[E] > [O] / ɣ_\n" +
				"[E] > [O] / _ɣ\n" +
				"ɣ ʕ > x ʔ\n" +
				"X > 0 / _X\n" +
				"X > 0 / _{i y í}\n" +
				"iX uX > ī ū\n" +

				"au eu ou am em om > ā ē ō ā ē ō / _m#\n" +
				"áu éu óu ám ém óm > â ê ô â ê ô / _m#\n" +

				"%%%% PHASE 02 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +
				"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +

				"u i > w y / X?_V\n"+


				"mr wr ml wl > br br bl bl / #_V\n" +

				"mX nX > mə nə / #_C\n" +

				"rX lX nX mX > r̩X l̩X n̩X m̩X / [OBSTRUENT]_\n" +

				"n m > n̩ m̩ / C_#\n" +
				"r l > r̩ l̩ / [OBSTRUENT]_{C #}\n" +
				"m n > m̩ n̩ / [OBSTRUENT]_{C #}\n" +
				"r̩ l̩ > r l / C_N{C #}\n" +

				"n̩X m̩X > ə̄    / _C\n" +
				"n̩X m̩X > ə̄n ə̄m / _{V #}\n" +

				"n̩ m̩ > ə     / _C\n" +
				"n̩ m̩ > ən əm / _{V #}\n" +

				"r̩X l̩X > ə̄r ə̄l   / _{C #}\n" +
				"r̩X l̩X > ər əl / _V\n" +
				"r̩ l̩ > ər əl\n" +

				"ə̄ > ū / {@K @Q}_\n" +
				"ə > u / {@K @Q}_\n" +

				"% LABIOVELAR SPLIT\n" +
				"@Q > @K / _{[O] [U] w [OBSTRUENT]}\n" +
				"@Q > @K /  {[O] [U] w [OBSTRUENT]}_\n" +
				"@Q > @P\n" +

				"%%%% PHASE 03 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +
				"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +

				"@VS > @VL / _X{C #}\n" +
				"@VS > @VL / _X{[I] [U] y w}V\n" +
				"@VS > @VL / _XV\n" +
				"X  > 0   / @VL_{C # u}\n" +
				"X  > 0   / [Obstruent]_V\n" +
				"X  > 0   / VA_V\n" +
				"xa xə > 0 / @VL_\n" +
				"xu xo > u / @VL_\n" +

				"ʔ > y / [E]_{[A] [O]}\n" +
				"ʔ > w / [O]_{[A] [E]}\n" +
				"x > w / [A]_[O]\n" +
				"x > w / [O]_[A]\n" +

				"% Desyllabification of Semivowels\n" +
				"i u > y w / _VC\n" +
				"iuiX uiuX > iwī uyū / C_\n" +

				"i u > y w / #_{@IUX @AE}\n" +
				"i u > y w / _@AE\n" +
				"i u > y w / @AE_@VL\n" +

				"i u > y w / {X C}_V\n" +
				"y w > i u / _{C X #}\n" +

				"@VSī @VSū > @VLi @VLu / _{C #}\n" +
				"@VSī @VSū > @VLy @VLw / _V\n" +
				"@VSī @VSū > @VLi @VLu / _{y w}\n" +

				"X > ə / {# [OBSTRUENT]}_C\n" +
				"X > ə / C_#\n" +
				"X > ə / A_A#\n" +
				"X > 0 / _{C #}\n" +
				"X > 0 / C_\n" +
				"ʔ  > 0 / #_\n" +

				"iʔə uʔə > ī ū\n" +
				"Xə > 0  / @VL_\n" +

				"% GRASSMANN'S LAW\n" +
				"@CH = pʰ tʰ cʰ kʰ\n" +
				"@J  = b  d  ɟ  g\n" +
				"@CH > @J / _R?VV?C*@CH\n" +

				"% Consonant Clusters\n" +
				"@Ks > ks\n" +
				"s > 0 / @KY_\n" +
				"tk tʰkʰ ct ɟt ck  > ks ks ɕt ɕt ɕk\n" +
				"tc dc tcʰ tʰcʰ > cc\n" +

				"s > 0 / _sss?\n" +
				"s > 0 / _s+C\n" +

				"oʔ@AE @AEʔo > ō ō\n" +
				"ʔ > 0  / {[E] [A] [O]}_{[I] [U]}\n" +

				"eʔe aʔa eʔa aʔe > ē ā ā ē\n" +
				"éʔe áʔa éʔa áʔe > ê â â ê\n" +
				"eʔé aʔá eʔá aʔá > ê â â ê\n" +

				"@WY = @IU W\n" +
				"aʔ eʔ oʔ > ā ē ō /_@WY\n" +
				"áʔ éʔ óʔ > â ê ô /_@WY\n" +

				"ēʔə êʔə   > ē ê\n" +
				"ēʔe êʔ[E] > ē ê\n" +
				"āʔe âʔ[E] > ā â\n" +
				"āʔa âʔ[A] > ā â\n" +
				"āxa âx[A] > ā â\n" +

				"oi ōi eu ēu > ai ei au ou\n" +
				"ói ôi éu êu > ái éi áu óu\n" +

				"n m > ən əm / CCC*_V\n" +
				"y w > iy uw / CCC*_V\n" +

				"ə̄  ə > ū u   / @K_K_";

		List<String> words = toList(
				"h₂oḱ-ri-",		  "bʰaḱehₐ-",
				"dʰh₁ilehₐ-",	  "ǵenh₁trihₐ-",
				"h₂rǵ-i-ḱuon-",	  "h₂wedh₂-",
				"dʰǵʰuhₓ",        "dʰǵʰem-en",
				"ǵʰes-l-dḱomth₂", "gʷyéh₃uyom",
				"tussḱyos",       "trh₂-we",
				"teuhₐ-",         "telh₂-",
				"somo-ǵn̩h₁-yo-s", "sem-s",
				"h₁óh₁es-",        "mlan"
								   );

		List<String> expected = toList(
				"xocri",		 "pʰacā",
				"tʰilā",		 "ɟentrī",
				"ərɟicwon",		 "əwetʰə",
				"ccū",           "ccemen",
				"cʰesəlccomtʰə", "byôuyom",
				"tusciyos",      "tə̄rwe",
				"tou",           "telə",
				"somoɟəyos",    "sēm",
				"ôwes",          "blan"
									  );

		SoundChangeApplier sca = new SoundChangeApplier(commands);
		List<Sequence> received = sca.processLexicon(words);

		assertEquals(toSequences(expected), received);
	}

    @Test
    public void testLoop01() throws RuleFormatException {
        String commands =
                "P = pw p t k\n" +
                        "B = bw b d g\n" +
                        "V = a o\n" +
                        "P > B / V_V\n" +
                        "P = p t k\n" +
                        "B = b d g\n" +
                        "B > 0 / #_c";

        SoundChangeApplier sca = new SoundChangeApplier(commands);

        sca.processLexicon(new ArrayList<String>());
    }

    @Test
    public void testDebug002() throws RuleFormatException {
        String commands =
                "AT = î\n" +
                "C  = þ s n\n" +
                "IN = ĕ\n" +
                "IN > 0 / ATC_#";

        SoundChangeApplier sca = new SoundChangeApplier(commands);

        List<String> list = new ArrayList<String>();

        list.add("þîsĕ");
        list.add("þîsnĕ");

        List<String> expected = toList(
                "þîs","þîsnĕ"
        );

        List<Sequence> received =  sca.processLexicon(list);

        assertEquals(toSequences(expected), received);
    }

	private List<Sequence> toSequences(List<String> strings) {
		List<Sequence> list = new ArrayList<Sequence>();

		for (String s : strings) {
			list.add(new Sequence(s));
		}
		return list;
	}

	private List<Sequence> toSequences(String string) {
		return toSequences(toList(string.split("\\s+")));
	}

	private List<String> toList(String... strings) {
		List<String> list = new ArrayList<String>();
		Collections.addAll(list,strings);
		return list;
	}
}
