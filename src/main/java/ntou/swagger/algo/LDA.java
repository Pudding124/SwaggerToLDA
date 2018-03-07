package ntou.swagger.algo;

/* 
 * Author: Hsuan-Ju Lin
 * Email: mis101bird@gmail.com
 * Github: https://github.com/mis101bird
 * 
 * Author: Ming-jen Xu 
 * Email: surprised128@gmail.com 
 * Github: https://github.com/Pudding124
 *  
 * Licensed under the MIT license. 
 * http://www.opensource.org/licenses/mit-license.php 
 * 
 */


import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;
import cc.mallet.topics.ParallelTopicModel;

import com.google.gson.*;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.net.URL;

// DEPENDS ON:
//   cc.mallet / mallet / 2.0.7

// CITE:
//   http://mallet.cs.umass.edu/topics-devel.php

// EXAMPLE:
/*
[
 [
  "The equipartition theorem is a formula from statistical mechanics that relates the temperature of a system with its average energies. The original idea of equipartition was that, in thermal equilibrium, energy is shared equally among its various forms; for example, the average kinetic energy in the translational motion of a molecule should equal the average kinetic energy in its rotational motion. Like the virial theorem, the equipartition theorem gives the total average kinetic and potential energies for a system at a given temperature, from which the system's heat capacity can be computed. However, equipartition also gives the average values of individual components of the energy. It can be applied to any classical system in thermal equilibrium, no matter how complicated. The equipartition theorem can be used to derive the classical ideal gas law, and the Dulong–Petit law for the specific heat capacities of solids. It can also be used to predict the properties of stars, even white dwarfs and neutron stars, since it holds even when relativistic effects are considered. Although the equipartition theorem makes very accurate predictions in certain conditions, it becomes inaccurate when quantum effects are significant, namely at low enough temperatures.",
  "The rings of Uranus were discovered on March 10, 1977, by James L. Elliot, Edward W. Dunham, and Douglas J. Mink. Two additional rings were discovered in 1986 by the Voyager 2 spacecraft, and two outer rings were found in 2003–2005 by the Hubble Space Telescope. A number of faint dust bands and incomplete arcs may exist between the main rings. The rings are extremely dark—the Bond albedo of the rings' particles does not exceed 2%. They are likely composed of water ice with the addition of some dark radiation-processed organics. The majority of Uranus's rings are opaque and only a few kilometres wide. The ring system contains little dust overall; it consists mostly of large bodies 0.2–20 m in diameter. The relative lack of dust in the ring system is due to aerodynamic drag from the extended Uranian exosphere—corona. The rings of Uranus are thought to be relatively young, at not more than 600 million years. The mechanism that confines the narrow rings is not well understood. The Uranian ring system probably originated from the collisional fragmentation of a number of moons that once existed around the planet. After colliding, the moons broke up into numerous particles, which survived as narrow and optically dense rings only in strictly confined zones of maximum stability."
 ],
 2
]
*/

@Component
public class LDA {
    
    private String[] defaultContractions;
    
    private int defaultNumIterations, defaultNumTopics, defaultNumThreads, defaultNumWords;
    
        // This is for backward compatibility. Accepts old LDA style inputs.
    public List<Map<String,Integer>> apply(String[] docs, int numTopics) throws IOException{
        
        defaultNumThreads = 1;
        defaultNumIterations = 100;
        defaultNumWords = 8;
        defaultNumTopics = numTopics;
        defaultContractions = new String[]{"a","able","about","above","according","accordingly","across","actually","after","afterwards","again","against","ain't","aint","ain´t","ain’t","all","allow","allows","almost","alone","along","already","also","although","always","am","among","amongst","an","and","another","any","anybody","anyhow","anyone","anything","anyway","anyways","anywhere","apart","appear","appreciate","appropriate","are","aren't","arent","aren´t","aren’t","around","as","aside","ask","asking","associated","at","available","away","awfully","b","be","became","because","become","becomes","becoming","been","before","beforehand","behind","being","believe","below","beside","besides","best","better","between","beyond","both","brief","but","by","c","came","can","can't","cannot","cant","can´t","can’t","cause","causes","certain","certainly","changes","clearly","co","com","come","comes","concerning","consequently","consider","considering","contain","containing","contains","corresponding","could","couldn't","couldnt","couldn´t","couldn’t","course","currently","d","definitely","described","despite","did","didn't","didnt","didn´t","didn’t","different","do","does","doesn't ","doesnt ","doesn´t ","doesn’t ","doing","don't","done","dont","don´t","don’t","down","downwards","during","e","each","edu","eg","eight","either","else","elsewhere","enough","entirely","especially","et","etc","even","ever","every","everybody","everyone","everything","everywhere","ex","exactly","example","except","f","far","few","fifth","first","five","followed","following","follows","for","former","formerly","forth","four","from","further","furthermore","g","get","gets","getting","given","gives","go","goes","going","gone","got","gotten","greetings","h","had","hadn't","hadnt","hadn´t","hadn’t","happens","hardly","has","hasn't","hasnt","hasn´t","hasn’t","have","haven't","havent","haven´t","haven’t","having","he","he'd","he'll","he's","hed","hell","hello","help","hence","her","here","here's","hereafter","hereby","herein","heres","hereupon","here´s","here’s","hers","herself","hes","he´d","he´ll","he´s","he’d","he’ll","he’s","hi","him","himself","his","hither","hopefully","how","how's","howbeit","however","hows","how´s","how’s","i","i'd","i'll","i'm","i've","id","ie","if","ignored","ill","im","immediate","in","inasmuch","inc","indeed","indicate","indicated","indicates","inner","insofar","instead","into","inward","is","isn't","isnt","isn´t","isn’t","it","it's","its","itself","it´s","it’s","ive","i´d","i´ll","i´m","i´ve","i’d","i’ll","i’m","i’ve","j","just","k","keep","keeps","kept","know","known","knows","l","last","lately","later","latter","latterly","least","less","lest","let","let's","lets","let´s","let’s","like","liked","likely","little","look","looking","looks","ltd","m","mainly","many","may","maybe","me","mean","meanwhile","merely","might","more","moreover","most","mostly","much","must","mustn't","mustnt","mustn´t","mustn’t","my","myself","n","name","namely","nd","near","nearly","necessary","need","needs","neither","never","nevertheless","new","next","nine","no","nobody","non","none","noone","nor","normally","not","nothing","novel","now","nowhere","o","obviously","of","off","often","oh","ok","okay","old","on","once","one","ones","only","onto","or","other","others","otherwise","ought","our","ours","ourselves","out","outside","over","overall","own","p","particular","particularly","per","perhaps","placed","please","plus","possible","presumably","probably","provides","q","que","quite","qv","r","rather","rd","re","really","reasonably","regarding","regardless","regards","relatively","respectively","right","s","said","same","saw","say","saying","says","second","secondly","see","seeing","seem","seemed","seeming","seems","seen","self","selves","sensible","sent","serious","seriously","seven","several","shall","shan't","shant","shan´t","shan’t","she","she'd","she'll","she's","shed","shell","shes","she´d","she´ll","she´s","she’d","she’ll","she’s","should","shouldn't","shouldnt","shouldn´t","shouldn’t","since","six","so","some","somebody","somehow","someone","something","sometime","sometimes","somewhat","somewhere","soon","sorry","specified","specify","specifying","still","sub","such","sup","sure","t","take","taken","tell","tends","th","than","thank","thanks","thanx","that","that's","thats","that´s","that’s","the","their","theirs","them","themselves","then","thence","there","there's","thereafter","thereby","therefore","therein","theres","thereupon","there´s","there’s","these","they","they'd","they'll","they're","they've","theyd","theyll","theyre","theyve","they´d","they´ll","they´re","they´ve","they’d","they’ll","they’re","they’ve","think","third","this","thorough","thoroughly","those","though","three","through","throughout","thru","thus","to","together","too","took","toward","towards","tried","tries","truly","try","trying","twice","two","u","un","under","unfortunately","unless","unlikely","until","unto","up","upon","us","use","used","useful","uses","using","usually","uucp","v","value","various","very","via","viz","vs","w","want","wants","was","wasn't","wasnt","wasn´t","wasn’t","way","we","we'd","we'll","we're","we've","wed","welcome","well","went","were","weren't","werent","weren´t","weren’t","weve","we´d","we´ll","we´re","we´ve","we’d","we’ll","we’re","we’ve","what","what's","whatever","whats","what´s","what’s","when","when's","whence","whenever","whens","when´s","when’s","where","where's","whereafter","whereas","whereby","wherein","wheres","whereupon","wherever","where´s","where’s","whether","which","while","whither","who","who's","whoever","whole","whom","whos","whose","who´s","who’s","why","why's","whys","why´s","why’s","will","willing","wish","with","within","without","won't","wonder","wont","won´t","won’t","would","wouldn't","wouldnt","wouldn´t","wouldn’t","x","y","yes","yet","you","you'd","you'll","you're","you've","youd","youll","your","youre","yours","yourself","yourselves","youve","you´d","you´ll","you´re","you´ve","you’d","you’ll","you’re","you’ve","z","zero"};;
        
        return run(docs);
    }
    
    public List<Map<String,Integer>> run(String[] documents) throws IOException {
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        List<String> inputs = new ArrayList<String>();
        for(String s : documents) {
            inputs.add(s.replaceAll("\\s+", " "));
        }

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        // pipeList.add( new StringList2FeatureSequence() );
        // pipeList.add( new CharSequenceArray2TokenSequence() );
        // pipeList.add( new TokenSequenceLowercase() );
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(false, false).addStopWords(defaultContractions) );  // Remove stopwords from a standard English stoplist. options: [case sensitive] [mark deletions]
        pipeList.add( new TokenSequence2FeatureSequence() );

        final InstanceList instances = new InstanceList(new SerialPipes(pipeList));

        // Load data from input strings to Instances
        for(String str : inputs) {
            Instance instance = new Instance(str, "target", "name", "source");
            instances.addThruPipe(instance);
        }

        // Create a model, alpha_t = 1.0, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        final ParallelTopicModel model = new ParallelTopicModel(defaultNumTopics, 1.0, 0.01);
        model.addInstances(instances);

        // Use two parallel samplers, where each one looks at one half the corpus and combine statistics after every iteration.
        model.setNumThreads(defaultNumThreads);

        // Run the model for ITERATIONS iterations and stop
        model.setNumIterations(defaultNumIterations);
        model.estimate();

        // OUTPUT
        List<Map<String,Integer>> output = new ArrayList<Map<String,Integer>>();

        if(model.getData().size()<=0){
            return output;
        }

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;
        
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for(int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        // System.out.println(out);
        
        // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        
        // Show top numWords words in topics with proportions for the first document
        for(int topic = 0; topic < defaultNumTopics; topic++) {
            
            HashMap<String,Integer> map = new HashMap<String,Integer>();
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < defaultNumWords) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                map.put(dataAlphabet.lookupObject(idCountPair.getID()).toString(), (int)idCountPair.getWeight());
                rank++;
            }
            
            ValueComparator bvc =  new ValueComparator(map);
            TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
            sorted_map.putAll(map);
            // System.out.println(out);
            output.add(sorted_map);
        }
        return output;
    }
    
    // for sorting the map by values
    private class ValueComparator implements Comparator<String> {

        Map<String, Integer> base;
        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
              return 1;
            }
        }
    }
}
