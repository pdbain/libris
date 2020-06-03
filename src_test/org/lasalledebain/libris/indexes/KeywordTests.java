package org.lasalledebain.libris.indexes;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Function;

import org.junit.Test;
import org.lasalledebain.libris.util.StringUtils;

public class KeywordTests {

	static final String[] iwadasn = {"It", "was", "a", "dark", "and",
	"stormy", "night", "the", "rain", "fell", "in", "torrents", "except",
	"at", "occasional", "intervals", "when", "it", "was", "checked", "by",
	"a", "violent", "gust", "of", "wind", "which", "swept", "up", "the",
	"streets", "for", "it", "is", "in", "London", "that", "our", "scene",
	"lies", "rattling", "along", "the", "housetops", "and", "fiercely",
	"agitating", "the", "scanty", "flame", "of", "the", "lamps", "that",
	"struggled", "against", "the", "darkness", "Through", "one", "of",
	"the", "obscurest", "quarters", "of", "London", "and", "among",
	"haunts", "little", "loved", "by", "the", "gentlemen", "of", "the",
	"police", "a", "man", "evidently", "of", "the", "lowest", "orders",
	"was", "wending", "his", "solitary", "way", "He", "stopped", "twice",
	"or", "thrice", "at", "different", "shops", "and", "houses", "of", "a",
	"description", "correspondent", "with", "the", "appearance", "of",
	"the", "quartier", "in", "which", "they", "were", "situated", "and",
	"tended", "inquiry", "for", "some", "article", "or", "another",
	"which", "did", "not", "seem", "easily", "to", "be", "met", "with",
	"All", "the", "answers", "he", "received", "were", "couched", "in",
	"the", "negative", "and", "as", "he", "turned", "from", "each", "door",
	"he", "muttered", "to", "himself", "in", "no", "very", "elegant",
	"phraseology", "his", "disappointment", "and", "discontent", "At",
	"length", "at", "one", "house", "the", "landlord", "a", "sturdy",
	"butcher", "after", "rendering", "the", "same", "reply", "the",
	"inquirer", "had", "hitherto", "received", "added", "But", "if",
	"this", "vill", "do", "as", "vell", "Dummie", "it", "is", "quite",
	"at", "your", "sarvice", "Pausing", "reflectively", "for", "a",
	"moment", "Dummie", "responded", "that", "he", "thought", "the",
	"thing", "proffered", "might", "do", "as", "well", "and", "thrusting",
	"it", "into", "his", "ample", "pocket", "he", "strode", "away", "with",
	"as", "rapid", "a", "motion", "as", "the", "wind", "and", "the",
	"rain", "would", "allow", "He", "soon", "came", "to", "a", "nest",
	"of", "low", "and", "dingy", "buildings", "at", "the", "entrance",
	"to", "which", "in", "halfeffaced", "characters", "was", "written",
	"Thames", "Court", "Halting", "at", "the", "most", "conspicuous", "of",
	"these", "buildings", "an", "inn", "or", "alehouse", "through", "the",
	"halfclosed", "windows", "of", "which", "blazed", "out", "in", "ruddy",
	"comfort", "the", "beams", "of", "the", "hospitable", "hearth", "he",
	"knocked", "hastily", "at", "the", "door", "He", "was", "admitted",
	"by", "a", "lady", "of", "a", "certain", "age", "and", "endowed",
	"with", "a", "comely", "rotundity", "of", "face", "and", "person",
	"Hast", "got", "it", "Dummie", "said", "she", "quickly", "as", "she",
	"closed", "the", "door", "on", "the", "guest", "Noa", "noa", "not",
	"exactly", "but", "I", "thinks", "as", "ow", "Pish", "you", "fool",
	"cried", "the", "woman", "interrupting", "him", "peevishly", "Vy",
	"it", "is", "no", "use", "desaving", "me", "You", "knows", "you",
	"has", "only", "stepped", "from", "my", "boosingken", "to", "another",
	"and", "you", "has", "not", "been", "arter", "the", "book", "at",
	"all", "So", "theres", "the", "poor", "cretur", "a", "raving", "and",
	"a", "dying", "and", "you", "Let", "I", "speak", "interrupted",
	"Dummie", "in", "his", "turn", "I", "tells", "you", "I", "vent",
	"first", "to", "Mother", "Bussblones", "who", "I", "knows", "chops",
	"the", "whiners", "morning", "and", "evening", "to", "the", "young",
	"ladies", "and", "I", "axes", "there", "for", "a", "Bible", "and",
	"she", "says", "says", "she", "I", "as", "only", "a", "Companion",
	"to", "the", "Halter", "but", "youll", "get", "a", "Bible", "I",
	"think", "at", "Master", "Talkins", "the", "cobbler", "as", "preaches",
	"So", "I", "goes", "to", "Master", "Talkins", "and", "he", "says",
	"says", "he", "I", "as", "no", "call", "for", "the", "Biblecause",
	"vy", "I", "as", "a", "call", "vithout", "but", "mayhap", "youll",
	"be", "a", "getting", "it", "at", "the", "butchers", "hover", "the",
	"vaycause", "vy", "The", "butcher", "ll", "be", "damned", "So", "I",
	"goes", "hover", "the", "vay", "and", "the", "butcher", "says", "says",
	"he", "I", "as", "not", "a", "Bible", "but", "I", "as", "a", "book",
	"of", "plays", "bound", "for", "all", "the", "vorld", "just", "like",
	"un", "and", "mayhap", "the", "poor", "cretur", "may", "nt", "see",
	"the", "difference", "So", "I", "takes", "the", "plays", "Mrs",
	"Margery", "and", "here", "they", "be", "surely", "And", "hows",
	"poor", "Judy", "Fearsome", "shell", "not", "be", "over", "the",
	"night", "Im", "a", "thinking" };

	@Test
	public void testTermFrequency() {
		HashSet<String> keyTerms = new HashSet<>();
		StringUtils.chooseTerms(Arrays.stream(iwadasn).map(t -> t.toLowerCase()), t -> 1, 10).forEach(t -> keyTerms.add(t));
		for (String k: new String[] {"the", "and", "a", "i", "of", "as", "he", "at", "to", "in"}) {
			assertTrue("missing term "+k, keyTerms.contains(k));
		}
	}

	@Test
	public void testTFIDF() {
		HashSet<String> keyTerms = new HashSet<>();
		final Function<String, Integer> documentFrequency = t -> {
			if (t.startsWith("win") || t.startsWith("butchers") || t.startsWith("hal")) return 1; else  return 20;
		};
		StringUtils.chooseTerms(Arrays.stream(iwadasn).map(t -> t.toLowerCase()), documentFrequency, 20).forEach(t -> keyTerms.add(t));
		for (String k: new String[] {"the", "butchers", "halfeffaced", "windows", "of", "as", "he", "at", "to", "in", "it", "you"}) {
			assertTrue("missing term "+k, keyTerms.contains(k));
		}
	}
}
