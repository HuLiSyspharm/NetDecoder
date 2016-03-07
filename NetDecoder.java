/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netdecoder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import static netdecoder.NetDecoderUtils.createNetworkFromPaths;
import static netdecoder.NetDecoderUtils.getFlowDifference;
import static netdecoder.NetDecoderUtils.getTotalFlowInProteins;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;


/**
 *
 * @author edroaldo
 * 
 */
public class NetDecoder {
    private CommandLine cmd = null;
    private Options options = null;
    
    private String file;
    //private String fileHGNC;
    private String geneSymbol;
    private String ontologyFile;
    
    
    public boolean checkArgs(String args[]){
        try{
            options = new Options();
            options.addOption("PPI", true, "PPI network");
            options.addOption("HGNC", true, "mappings to official gene symbols");
            options.addOption("SYMBOL", true, "mappings between protein ids, gene symbols and gene ontologies");
            options.addOption("GO", true, "The Gene Ontology");
            
            options.addOption("gen", false, "generate patient-specific networks");
            options.addOption("randomizations", true, "num of randomizations to perform");
            
            options.addOption("comp", false, "generate composite network by integrating patient-specific networks");
            options.addOption("e", true, "gene expression matrix");
            options.addOption("s", true, "table contaning sample annotation");
            options.addOption("d", true, "type of network to generate, e.g, control or ERnegative");
            options.addOption("control", true, "control annotation from the sample table to select control arrays");
            options.addOption("condition", true, "disease annotation from the sample table to select control arrays");
            options.addOption("g", true, "gene list to be used as sources");
            options.addOption("i", true, "file containing irrelevant Gene Ontologies");
            options.addOption("out", true, "output folder to save patient-specific networks");
            options.addOption("in", true, "folder containig patient-specific networks");
            options.addOption("f", true, "filename");
            options.addOption("nc", true, "network control");
            options.addOption("ncp", true, "network control paths");
            options.addOption("nd", true, "network disease");
            options.addOption("ndp", true, "network disease paths");
            options.addOption("lfm", true, "load filtered matrix of key interactions");
            options.addOption("H", false, "Hub analysis");
            
            
            options.addOption("S", false, "Evaluate the significance of predicted key edges");
            
            options.addOption("E", false, "Edge analysis");
            options.addOption("corThreshold", true, "correlation threshold for key edges");
            options.addOption("ratioThreshold", true, "ratio threshold for key edges");
            options.addOption("ratioSink", true, "ratio to select sinks with flow differences");
            options.addOption("ratioHidden", true, "ratio to select hidden proteins with flow differences");
            options.addOption("top", true, "top sinks or hidden proteins");
            options.addOption("overlap", false, "Remove overlap control and disese edges");
            options.addOption("includeKeyEdges", false, "Prioritize paths including key edges in addition to include hidden and sinks with highest flow differences");
            
            options.addOption("P", false, "Path analysis");
            options.addOption("PD", false, "Path analysis with DEGs");
            options.addOption("thresholdFlow", true, "Threshold for flow");
            options.addOption("thresholdDistance1", true, "Threshold for euclidean distance, similarity");
            options.addOption("thresholdDistance2", true, "Threshold for euclidean distance, dyssimilarity");
            
            options.addOption("frequency", true, "Use frequency to filter edges");
            
            options.addOption("C", false, "Evaluate context contribution of disease-associated genes");
            options.addOption("gwGOscores", true, "load genome-wide GO scores");
            options.addOption("geneGOscores", true, "load gene GO scores");
            options.addOption("ds", true, "list of disease states");
        
            options.addOption("gGO", false, "calculate GO scores for disease-associated genes");
            options.addOption("GRN", true, "Gene regulatory network used to defined a context for disease-associated genes");
            
            options.addOption("GWGO", false, "generate genome-wide GO scores");
            options.addOption("sourceGOscores", true, "file containing GO scores generated from source genes defined by the user (top 100, for example)");
            
            options.addOption("UGL", false, "run NetDecoder using a user-defined gene list as sources");
            
            options.addOption("PKS", false, "Predict key source genes");
            options.addOption("PKP", false, "Predic key processes");
            
            options.addOption("GN", false, "Evaluate DFNs in different contexts");
            
            options.addOption("DFN", false, "Obtain two differential networks from two networks");
            
            options.addOption("topPATHS", false, "Create subnetworks from top paths in DFNs");
            
            options.addOption("RANDOM", false, "Create composite networks using a randomized PPI");
            options.addOption("rPPI", true, "Random PPI");
            
            options.addOption("rr", true, "Indicate if real or random expression is used");
            
            options.addOption("ccs", true, "list of CCS files, space separated");
            options.addOption("cCCS", false, "combine Context-Contribution Scores");
            options.addOption("hCCS", true, "Threshold to select high CCS > 10");
            options.addOption("lCCS", true, "Threshold to select low CCS (< -10)");
            options.addOption("variance", true, "Variance to filter CCS (> 50)");
            
            options.addOption("V", false, "Run a validation analysis");
            
            
            //options.addOption("generate_random_sources", false, "generate random sources");
            //options.addOption("random_sources", true, "file containing random sources");
            //options.addOption("num_random_sources", true, "number of random sources to generate");
            //options.addOption("num_iterations", true, "number of iterations");
        
            CommandLineParser parser = new PosixParser();
            this.cmd = parser.parse(options, args);

            return true;
        }catch(Exception e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return false;
    }
    
    public void runAnalysis() throws Exception, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        file = cmd.getOptionValue("PPI");
        //fileHGNC = cmd.getOptionValue("HGNC");
        geneSymbol = cmd.getOptionValue("SYMBOL");
        ontologyFile = cmd.getOptionValue("GO");

        if (cmd.hasOption("gen")) {
            generatePhenotypeNetworks();
        }else if(cmd.hasOption("RANDOM")){
            generateRandomNetworks();
        }else if(cmd.hasOption("E")){
            edgeAnalysis(); 
            System.exit(0);
        }else if(cmd.hasOption("S")){
            FOXM1();
            //computePval();
            //applyEdgeAnalysis2RandomNetworks();
            //computeCCSRandomNetworks(); 
            System.exit(0);
        }else if(cmd.hasOption("C")){
            //impactScore();
            System.exit(0);
        }else if (cmd.hasOption("cCCS")){
            if(cmd.hasOption("RANDOM")){
                //significantCCS();
            }else{
                plotIP();
            }
            System.exit(0);
        }else if(cmd.hasOption("V")){
            validation();
            System.exit(0);
        }
    }
    
    public void validation() throws IOException {
    }
    
    //use edge flow for each edge in a path_dir as a test statistic?
    //or use total flow through a path_dir as a test statistic?
    public void computePval() throws Exception{ //CONTINUE LATER...
        String dir = cmd.getOptionValue("out");
        //String d = cmd.getOptionValue("d");
        String filename = cmd.getOptionValue("f");
        String ncp = cmd.getOptionValue("nc");
        String ndp = cmd.getOptionValue("nd");
        
        String condition = cmd.getOptionValue("condition");
        //RJava rJava = new RJava();
        
        List<String> controlPaths = Serialization.deserialize(ncp, List.class);
        List<String> diseasePaths = Serialization.deserialize(ndp, List.class);
        List<Path> cPaths = NetDecoderUtils.convertString2Paths(controlPaths);
        List<Path> dPaths = NetDecoderUtils.convertString2Paths(diseasePaths);
        
        System.out.println(controlPaths.size());
        System.out.println(diseasePaths.size());
        System.out.println(dPaths.size());
        String ref = "RandomNetwork_ERnegative_Sources";
        
        List<List<Path>> allRandomPaths = new ArrayList();
        for (int i = 0; i < 50; i++) { //50 randomizations
            String path_dir = dir + condition + "/random/random_" + i + "/";
            String open = path_dir + ref;
            //System.out.println("Opening file in " + open);
            List<String> randomPathsAux = Serialization.deserialize(open + "_paths.ser", List.class);
            List<Path> randomPaths = NetDecoderUtils.convertString2Paths(randomPathsAux);
            allRandomPaths.add(randomPaths);
        }
        List<Path> significantPaths = new ArrayList();
        for (Path path : dPaths) {
            double countHigher = 0;
            for (int i = 0; i < 50; i++) { //50 randomizations
                List<Path> randomPaths = allRandomPaths.get(i);
                double rPathFlow = 0; //if a a random network do not contains an observed path, its path flow in zero.
                if(randomPaths.contains(path)){
                    int index = randomPaths.indexOf(path);
                    Path rPath = randomPaths.get(index);
                    rPathFlow = rPath.getFlow();
                }
                if (rPathFlow >= path.getFlow()) {
                    countHigher++;
                }
            }
            double pvalueHigher = countHigher / 50.0;
            if (pvalueHigher < 0.05) {
                significantPaths.add(path);
                System.out.println(path);
                System.out.println("higher flow: " + pvalueHigher);
                System.out.println("-----------");
            }
        }
        System.out.println("significant paths: " + significantPaths.size() + " of " + dPaths.size());
    }

    //Figure out a way to compute p-values using flow difference as test statistic
    public void computePval_TMP_OLD() throws Exception{ //CONTINUE LATER...
        String dir = cmd.getOptionValue("out");
        //String d = cmd.getOptionValue("d");
        String filename = cmd.getOptionValue("f");
        String nc = cmd.getOptionValue("nc");
        String nd = cmd.getOptionValue("nd");
        
        String condition = cmd.getOptionValue("condition");
        //RJava rJava = new RJava();
        
        Map<String, Node> controlNetwork = Serialization.deserialize(nc, Map.class);
        Map<String, Node> diseaseNetwork = Serialization.deserialize(nd, Map.class);
        Map<String, Map<String, Double>> flowInNetworks = getFlowInNetworks(controlNetwork, diseaseNetwork);
        
        //Map<String, Double> diseaseSinksMap = NetDecoderUtils.getSinks(diseasePaths);
        //Map<String, Double> obsFlowDifference = getFlowDifference(flowInNetworks, diseaseSinksMap.keySet());
        Map<String, Double> obsFlowDifference = computeImpactScore(controlNetwork, diseaseNetwork);
        
        System.out.println(controlNetwork.size());
        System.out.println(diseaseNetwork.size());
        System.out.println(obsFlowDifference.size());
        String ref = "RandomNetwork_ERnegative_Sources";
        for (String gene : obsFlowDifference.keySet()) {
            System.out.println(gene);
            double countHigher = 0;
            double countLower = 0;
            double total = 0;
            for (int i = 0; i < 50; i++) { //50 randomizations
                String path = dir + condition + "/random/random_" + i + "/";
                String open = path + ref;
                //System.out.println("Opening file in " + open);
                Map<String, Node> randomNetwork = Serialization.deserialize(open + "_subnet.ser", Map.class);
                //Map<String, Double> randomSinksMap = NetDecoderUtils.getSinks(randomPaths);
                //Map<String, Map<String, Double>> randFlowInNetworks = getFlowInNetworks(controlPaths, randomPaths);
                //Map<String, Double> randFlowDifference = getFlowDifference(randFlowInNetworks, randomSinksMap.keySet());
                Map<String, Double> randFlowDifference = computeImpactScore(controlNetwork, randomNetwork);
                
                if (randFlowDifference.containsKey(gene)) {
                    if (randFlowDifference.get(gene) >= obsFlowDifference.get(gene)) {
                        countHigher++;
                    }
                    if(randFlowDifference.get(gene) <= obsFlowDifference.get(gene)){
                        countLower++;
                    }
                }else{
                    System.out.println("it is not a sink in random network...");
                }
            }
            double pvalueHigher = countHigher / 50.0;
            double pvalueLower = countLower / 50.0;
            System.out.println(pvalueHigher);
            System.out.println(pvalueLower);
            /*System.out.println("---counts---");
            System.out.println(countHigher);
            System.out.println(countLower);*/
            
            System.out.println("-----------");
        }
    }

    //assessing significance of key edges and top hidden/sinks
    //first apply an edge analysis in each random network... then count!
    public void applyEdgeAnalysis2RandomNetworks() throws Exception{ //CONTINUE LATER...
        String dir = cmd.getOptionValue("out");
        //String d = cmd.getOptionValue("d");
        String filename = cmd.getOptionValue("f");
        String nc = cmd.getOptionValue("nc");
        String ncp = cmd.getOptionValue("ncp");
        String control = cmd.getOptionValue("control");
        String condition = cmd.getOptionValue("condition");
        Double corThreshold = Double.valueOf(cmd.getOptionValue("corThreshold"));
        Double ratioThreshold = Double.valueOf(cmd.getOptionValue("ratioThreshold"));
        Double ratioSink = Double.valueOf(cmd.getOptionValue("ratioSink"));
        Double ratioHidden = Double.valueOf(cmd.getOptionValue("ratioHidden"));
        Integer top = Integer.valueOf(cmd.getOptionValue("top"));
        
        RJava rJava = new RJava();
        
        Map<String, Node> controlNetwork = Serialization.deserialize(nc, Map.class);
        //List<String> controlNetworkPaths = Serialization.deserialize(ncp, List.class);
        //NEED TO FIGURE OUT HOW TO ITERATE THROUGH ALL NETWORKS/PATHS FROM THE FOLDERS...
        String ref = "RandomNetwork_ERnegative_Sources";
        for (int i = 0; i < 50; i++) { //50 randomizations
            String path = dir + condition + "/random/random_" + i + "/";
            String open = path + ref;
            System.out.println("Opening file in " + open);
            
            Map<String, Node> diseaseNetwork = Serialization.deserialize(open + "_subnet.ser", Map.class);
            //List<String> diseaseNetworkPaths = Serialization.deserialize(open + "_paths.ser", List.class);

            Map<String, Map<String, Double>> flowInNetworks = getFlowInNetworks(controlNetwork, diseaseNetwork);

            Set<Edge> cEdges = getAllEdges(controlNetwork);
            Set<Edge> dEdges = getAllEdges(diseaseNetwork);
            Map<String, Map<String, Double>> flowMatrix = new LinkedHashMap();

            if (!cmd.hasOption("overlap")) { //use only edges in both networks to infer key edges?
                dEdges.addAll(cEdges);
            } else {
                dEdges.retainAll(cEdges);
            }
            Map<String, Double> xxControl = new LinkedHashMap();
            Map<String, Double> xxDisease = new LinkedHashMap();
            
            for (Edge e : dEdges) {
                Edge eControl = NetDecoderUtils.getEdge(controlNetwork, e);
                Edge eDisease = NetDecoderUtils.getEdge(diseaseNetwork, e);
                if (eControl != null && eDisease != null) {
                    xxControl.put(e.toString(), eControl.getFlow());
                    xxDisease.put(e.toString(), eDisease.getFlow());
                } else if (eControl != null) {
                    xxControl.put(e.toString(), eControl.getFlow());
                    xxDisease.put(e.toString(), 0.0);
                } else if (eDisease != null) {
                    xxControl.put(e.toString(), 0.0);
                    xxDisease.put(e.toString(), eDisease.getFlow());
                }
            }
            flowMatrix.put(control, xxControl);
            flowMatrix.put(condition, xxDisease);
            Map<String, Map<String, Double>> aux = changeMapping(flowMatrix);
            String name = path + filename + "_flowMatrix";
            saveFlowMatrix(aux, control, condition, name + ".txt");
            rJava.plotBarplot(path, name, condition, corThreshold, ratioThreshold, filename);
        }
        
        countFeatures(dir, filename, condition);
    }
    
    public void countFeatures(String dir, 
            String filename, 
            String condition) throws Exception{
        //String dir = cmd.getOptionValue("in");
        //String filename = cmd.getOptionValue("f");
        //String control = cmd.getOptionValue("control");
        //String condition = cmd.getOptionValue("condition");
        String rKeyEdges = dir + condition + "/"+ filename + "_keyEdges.txt";
        System.out.println(rKeyEdges);
        Set<Edge> realKeyEdges = loadKeyEdges(rKeyEdges);
        Map<String, Integer> countKeyEdges = new LinkedHashMap();
        for(Edge ed:realKeyEdges){
            countKeyEdges.put(ed.toString(), 0);
        }
        
        for (int i = 0; i < 50; i++) { //50 randomizations
            String random = "/random/random_" + i;
            String path = dir + condition + random + "/";
            String keyEdgesFile = path + filename + "_keyEdges.txt";
            Set<Edge> keyEdges = loadKeyEdges(keyEdgesFile);
            //allKeyEdges.put(random, keyEdges);
            for(Edge e:keyEdges){
                if(countKeyEdges.containsKey(e.toString())){
                    countKeyEdges.put(e.toString(), countKeyEdges.get(e.toString()) + 1);
                }
            }
        }
        System.out.println(countKeyEdges);
    }

    public List<Double> getRandomCorrelations(List<Double> correlations){
        List<Double> random = new ArrayList();
        int max = correlations.size();
        int min = 0;
        for(int i = 0; i < correlations.size(); i++){
            Random rand = new Random();
            int randomNumber = rand.nextInt((max - min) + min);
            
            random.add(correlations.get(randomNumber));
        }
        return random;
    }
    
    public void generateRandomNetworks() throws IOException {
        System.out.println("Random networks...");
        String geneListFile = cmd.getOptionValue("g");
        String state = cmd.getOptionValue("d");
        String filename = cmd.getOptionValue("f");
        int numIterations = Integer.valueOf(cmd.getOptionValue("randomizations"));
        String dir = cmd.getOptionValue("out");
        OntologyUtils utils = new OntologyUtils(ontologyFile);
        NetworkFlow net = new NetworkFlow();

        InputOutput io = new InputOutput();
        io.getColumnFromMITABFaster(file);
        List<String> TFAnnot = setDefaultSinks();

        Set<String> sourceGenes = new LinkedHashSet(NetworkFlow.getGenes(geneListFile));
        Set<String> sinks = net.setUniversalSinksHuman(utils, TFAnnot, geneSymbol);
        List<List<Node>> sourcesAndSinks = net.selectSources(sourceGenes, sinks, io.getProteinsA(),
                io.getProteinsB());
        List<Node> sources = sourcesAndSinks.get(0); //0 stores the sources
        List<Node> sinkNodes = sourcesAndSinks.get(1); //1 stores the sinks

        for (int i = 0; i < numIterations; i++) {
            String path2create = dir + state + "/random_" + i +"/";
            new File(path2create).mkdirs(); //create folders
            System.out.println("saving data into: " + path2create);
            
            List<Double> correlations = getRandomCorrelations(io.getCorrelations());//io.getCorrelations(); //get random numbers between 0 and 1
            List<Double> signCors = io.getSignCors();
            
            Map<Integer, Map<String, Edge>> allPathsNet = new LinkedHashMap();
            Set<String> sinkGOSubnet = new LinkedHashSet();
            Map<String, Node> network = new LinkedHashMap();
            
            net.createNetwork(sources, sinkNodes, network,
                    io.getProteinsA(), io.getProteinsB(), correlations, signCors);

            net.mappingNameToId(network);
            net.setGOHuman(network, utils, geneSymbol);
            net.compute(network, allPathsNet, utils, sinkGOSubnet);
            Map<String, Node> subnet = new LinkedHashMap();
            Map<Integer, List<Edge>> pathsSubnet = net.buildSubnet(network,
                    subnet, allPathsNet);

            //Map<Integer, List<Edge>> pathsSubnet = net.getPaths(auxNetMP, subnet);
            List<String> pathsFlow = new ArrayList();
            for (Integer path : pathsSubnet.keySet()) {
                String pathFlowAsString = NetDecoderUtils.convertPath2String(pathsSubnet.get(path), true);
                pathsFlow.add(pathFlowAsString);
            }
            String subnetName = filename + "_subnet.ser";
            String pathsName = filename + "_paths.ser";
            Serialization.serialize(subnet, path2create + subnetName);
            Serialization.serialize(pathsFlow, path2create + pathsName);

            Map<String, Double> GOscoreW = NetworkFlow.countGOWeighted(subnet);
            NetworkFlow.saveGOscoresW(GOscoreW, state, utils, path2create + filename + ".txt");
        }
    }

    public void generatePhenotypeNetworks() throws IOException{
        System.out.println("Phenotype-specific networks...");
        String geneListFile = cmd.getOptionValue("g");
        String state = cmd.getOptionValue("d");
        String filename = cmd.getOptionValue("f");
        String dir = cmd.getOptionValue("out");
        OntologyUtils utils = new OntologyUtils(ontologyFile);
        NetworkFlow net = new NetworkFlow();
        
        InputOutput io = new InputOutput();
        io.getColumnFromMITABFaster(file);
        List<String> TFAnnot = setDefaultSinks();

        Set<String> sourceGenes = new LinkedHashSet(NetworkFlow.getGenes(geneListFile));
        List<Double> correlations = io.getCorrelations();
        List<Double> signCors = io.getSignCors();
        
        Map<Integer, Map<String, Edge>> allPathsNet = new LinkedHashMap();
        Set<String> sinkGOSubnet = new LinkedHashSet();
        
        Map<String, Node> network = new LinkedHashMap();
        Set<String> sinks = net.setUniversalSinksHuman(utils, TFAnnot, geneSymbol);
        
        List<List<Node>> sourcesAndSinks = net.selectSources(sourceGenes, sinks, io.getProteinsA(),
                io.getProteinsB());
        List<Node> sources = sourcesAndSinks.get(0);
        List<Node> sinkNodes = sourcesAndSinks.get(1);
        
        net.createNetwork(sources, sinkNodes, network,
                io.getProteinsA(), io.getProteinsB(), correlations, signCors);

        net.mappingNameToId(network);
        net.setGOHuman(network, utils, geneSymbol);
        net.compute(network, allPathsNet, utils, sinkGOSubnet);
        Map<String, Node> subnet = new LinkedHashMap();
        Map<Integer, List<Edge>> pathsSubnet = net.buildSubnet(network, 
                subnet, allPathsNet);

        //Map<Integer, List<Edge>> pathsSubnet = net.getPaths(auxNetMP, subnet);
        List<String> pathsFlow = new ArrayList();
        for (Integer path : pathsSubnet.keySet()) {
            String pathFlowAsString = NetDecoderUtils.convertPath2String(pathsSubnet.get(path), true);
            pathsFlow.add(pathFlowAsString);
        }
        //String subnetName = filename + "_subnet.ser";
        String pathsName = filename + "_paths.ser";
        //Serialization.serialize(subnet, dir + subnetName);
        Serialization.serialize(pathsFlow, dir + pathsName);
        //(new NetworkFlow()).saveSubNet(subnet, filename+"_subnet.txt");
        
        //Map<String, Double> GOscoreW = NetworkFlow.countGOWeighted(subnet);
        //NetworkFlow.saveGOscoresW(GOscoreW, state, utils, dir + filename + ".txt");
    }
    
    public Map<String, Map<String, Double>> changeMapping(
            Map<String, Map<String, Double>> ccsMap){
    
        //gene -> matrix -> score
        Map<String, Map<String, Double>> aux = new LinkedHashMap();
        for(String s:ccsMap.keySet()){
            for(String go:ccsMap.get(s).keySet()){
                Map<String, Double> aux2 = new LinkedHashMap();
                aux2.put(s, ccsMap.get(s).get(go));
                if(!aux.containsKey(go)){
                    aux.put(go, aux2);
                }else{
                    aux.get(go).put(s, aux2.get(s));
                }
            }
        }
        return aux;
    }
    
    public void plotIP() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
        //open several files containing all context contribution scores for a specific phenotype
        String[] ccs = cmd.getOptionValue("ccs").split("\\ ");
        List<String> diseases = Arrays.asList(cmd.getOptionValue("ds").split("\\ "));
        String filename = cmd.getOptionValue("f");
        String dir = cmd.getOptionValue("out");
        
        /*Map<String, Map<String, Double>> ccsMap = new LinkedHashMap();
        for(int i = 0; i < ccs.length; i++){
            System.out.println(diseases.get(i) + " " + loadCCS(ccs[i]));
            ccsMap.put(diseases.get(i), loadCCS(ccs[i]));
        }
        //System.out.println(ccsMap);
        Map<String, Map<String, Double>> CCS = changeMapping(ccsMap);
        System.out.println(CCS);*/
        /*for (String path_dir : CCS.keySet()) {
            if (CCS.get(path_dir).size() < diseases.size()) { //in case the state is not present in all CCS profiles.
                for (int i = 1; i < ccs.length; i++) {
                    CCS.get(path_dir).put(diseases.get(i), 0.0);
                }
            }
        }*/
        //saveCCSMatrix(CCS, filename+".txt");
        
        RJava rJava = new RJava();
        if(diseases.size() == 2){
            //rJava.plotHeatmap_CCS(dir, ccs[0], ccs[1], filename, diseases.get(0), diseases.get(1));
            rJava.createScritpHeatmap_CCS(dir, ccs[0], ccs[1], filename, diseases.get(0), diseases.get(1));
        }else{
            rJava.plotHeatmap_CCS_2(dir, ccs[0], ccs[1], ccs[2], filename, diseases.get(0), diseases.get(1), diseases.get(2));
        }
        //rJava.plotHeatmap_CCS_2(dir, ccs[0], "", filename, diseases.get(0), "");
        //rJava.plotHeatmap_Fig4(dir, filename);
    }
    
    public static void saveCCSMatrix(
            Map<String, Map<String, Double>> ccsMatrix,
            String file) throws IOException{
        
            File f = new File(file);
            if(!f.exists()){
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            
            int i = 0;
            bw.write("gene");
            for (String gene : ccsMatrix.keySet()) {
                Map<String, Double> ccs = ccsMatrix.get(gene);
                if(i == 0){
                    for(String matrix:ccs.keySet()){
                        bw.write("\t" + matrix);
                        //bw.write(edge.getSymbol() + "\t");
                    }
                    bw.newLine();
                    i = 1;
                }
                //bw.write(state.getSymbol() + "\t");
                bw.write(gene);
                for (String matrix : ccs.keySet()) {
                    bw.write("\t" + ccs.get(matrix));
                }
                bw.newLine();
            }
            bw.close();
    }
    
    public static Map<String, Double> loadCCS(String file) throws IOException{
        
        Map<String, Double> ccs = new LinkedHashMap();
        BufferedReader in = null;
        //int edgesColumn = 1;
        //int scoreColumn = 3;
        try{
            in  = new BufferedReader(new FileReader(file));
            String firstLine = in.readLine();
            //System.out.println(firstLine);
            String line;
        
            while((line = in.readLine()) != null){
                String[] columns = line.split("\t");
                String gene = columns[1];
                Double score = Double.valueOf(columns[2]);
                ccs.put(gene, score);
            }
            return ccs;
        }
        finally {
        	in.close();
        }
    }
    
    /*
    public void computeCCSRandomNetworks() throws Exception{ //CONTINUE LATER...
        String[] gwGOscore = cmd.getOptionValue("gwGOscores").split("\\ ");
        String[] geneGOscore = cmd.getOptionValue("geneGOscores").split("\\ ");
        //List<String> diseaseStates = Arrays.asList(cmd.getOptionValue("ds").split("\\ "));
        String realRandom = cmd.getOptionValue("rr");
        String dir = cmd.getOptionValue("in");
        String condition = cmd.getOptionValue("condition");
        String filename = cmd.getOptionValue("f");
        
        List<Map<String, Map<String, Double>>> geneContext = new ArrayList();
        
        geneContext.add(Serialization.deserialize(geneGOscore[0], Map.class));
        geneContext.add(Serialization.deserialize(geneGOscore[1], Map.class));
        
        String ref = "RandomNetwork_ERnegative_Sources";
        for(int i = 0; i < 50; i++){
            String state = "random_" + i;
            List<String> diseaseStates = new ArrayList();
            diseaseStates.add("control");
            diseaseStates.add(state);
            String path_dir = dir + condition + "/random/random_" + i + "/";
            String open = path_dir + ref;
            System.out.println(dir);
            System.out.println("Opening file in " + open);
            List<Map<String, Double>> genomeContext = new ArrayList();
            Map<String, Double> auxGW = NetworkFlow.loadGOscores(open + ".txt");
            genomeContext.add(NetworkFlow.loadGOscores(gwGOscore[0]));
            genomeContext.add(auxGW);
            
            NetDecoderUtils.contributionScore(genomeContext, geneContext, diseaseStates,
                realRandom, path_dir + filename);
        }
        
    }*/
    
    public Set<Edge> getAllEdges(Map<String, Node> network) {
        Set<Edge> edges = new LinkedHashSet();
        for (String p : network.keySet()) {
            Node protein = network.get(p);
            for (Edge edge : protein.getEdges()) {
                if (edge.getFrom().getName().equals("s")
                        || edge.getTo().getName().equals("t")) {
                    continue;
                }
                edges.add(edge);
            }
        }
        return edges;
    }
    public static void saveFlowMatrix(
            Map<String, Map<String, Double>> flowMatrix,
            String control,
            String disease,
            String file) throws IOException {

        File f = new File(file);
        if (!f.exists()) {
            f.createNewFile();
        }
        FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write("edge\t" + control + "\t" + disease);
        bw.newLine();
        for (String edge : flowMatrix.keySet()) {
            bw.write(edge);
            Map<String, Double> states = flowMatrix.get(edge);
            for(String state:states.keySet()){
                bw.write("\t" + states.get(state));
            }
            bw.newLine();
        }
        bw.close();
    }

    public static Set<Edge> loadKeyEdges(String file) throws IOException{
        Set<Edge> keyEdges = new LinkedHashSet();
        
        BufferedReader in = null;
        int edgesColumn = 0;
        try{
            in  = new BufferedReader(new FileReader(file));
            in.readLine();
            String line;
            while((line = in.readLine()) != null){
                String[] columns = line.split("\t");
                String[] xx = columns[edgesColumn].split("\\->");
                Edge edge = new Edge(new Node(xx[0]), new Node(xx[1]));
                keyEdges.add(edge);
            }
            return keyEdges;
        }
        finally {
        	in.close();
        }
    }

    public Map<String, Double> computeImpactScore(
            Map<String, Node> controlNetwork, 
            Map<String, Node> diseaseNetwork){
        
        Map<String, Map<String, Double>> flowInNetworks = getFlowInNetworks(controlNetwork, diseaseNetwork);
        
        Map<String, Double> scores = new LinkedHashMap();
        Map<String, Double> sources = NetDecoderUtils.getSources(diseaseNetwork);
        for (String gene : flowInNetworks.keySet()) {
        //for (String path_dir : candidateGenes) {
            if (!(sources.containsKey(gene) || gene.equals("s") || gene.equals("t"))) {
                if (controlNetwork.containsKey(gene) && diseaseNetwork.containsKey(gene)) {
                    double numRI = rewiredInteractions(gene, controlNetwork, diseaseNetwork);
                    int numCC = correlationChange(gene, controlNetwork, diseaseNetwork);
                    //double score = Math.log10((Math.abs(flowInNetworks.get(path_dir).get("difference")) * numRI * numCC));
                    double score = flowInNetworks.get(gene).get("difference") * numRI * numCC;
                    scores.put(gene, score);
                    //System.out.println(path_dir + "\t" + score +"\t"  + numRI + "\t" + numCC + "\t" + flowInNetworks.get(path_dir).get("difference"));
                }
            }
        }
        Map<String, Double> sortedScores = NetworkFlow.sortByValues(scores);
        List<String> aux = new ArrayList(sortedScores.keySet());
        List<String> topGenes_down = aux.subList(0, 20);
        List<String> topGenes_up = aux.subList(aux.size() - 20, aux.size());
        List<String> topGenes = new ArrayList(topGenes_down);
        topGenes.addAll(topGenes_up);
        Map<String, Double> topGenesScores = new LinkedHashMap();
        for(String g:topGenes){
            //topGenesScores.put(g, Math.log10(sortedScores.get(g)));
            if(sortedScores.get(g) > 0){
                topGenesScores.put(g, Math.log10(sortedScores.get(g)));
            }else{
                double tmp = -Math.log10(Math.abs(sortedScores.get(g)));
                topGenesScores.put(g, tmp);
            }
        }
        return topGenesScores;
    }
    
    public Map<String, Double> getEdgeFlow(Map<String, Node> network){
        Map<String, Double> edgeFlows = new LinkedHashMap();
        Set<Edge> edges = getAllEdges(network);
        for(Edge e:edges){
            edgeFlows.put(e.toString(), e.getFlow());
        }
        return edgeFlows;
    }
    
    public void impactScore(String path2create) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String dir = cmd.getOptionValue("out");
        String filename = cmd.getOptionValue("f");
        //List<String> diseaseStates = Arrays.asList(cmd.getOptionValue("ds").split("\\ "));
        //String geneListFile = cmd.getOptionValue("g");
        String control = cmd.getOptionValue("control");
        String condition = cmd.getOptionValue("condition");
        
        String ncp = cmd.getOptionValue("ncp");
        String ndp = cmd.getOptionValue("ndp");
        RJava rJava = new RJava();
        
        List<String> controlNetworkPaths = Serialization.deserialize(ncp, List.class);
        List<String> diseaseNetworkPaths = Serialization.deserialize(ndp, List.class);
        Map<String, Node> controlNetwork = createNetworkFromPaths(controlNetworkPaths);
        Map<String, Node> diseaseNetwork = createNetworkFromPaths(diseaseNetworkPaths);
        Map<String, Double> topGenesScores = computeImpactScore(controlNetwork, diseaseNetwork);
        
        //String path2create = dir + diseaseStates.get(1) + "/";
        //String path2create = dir + condition + "/";
        //new File(path2create).mkdirs();
        Map<String, Map<String, Double>> flowDifference = getFlowInNetworks(controlNetwork, diseaseNetwork);
        Map<String, Double> totalFlowsDisease = NetDecoderUtils.getTotalFlow(diseaseNetwork);
        Map<String, Double> totalFlowsControl = NetDecoderUtils.getTotalFlow(controlNetwork);
        
        List<String> Rscripts = new ArrayList();
        for (String gene : topGenesScores.keySet()) {
            List<Edge> controlEdges = controlNetwork.get(gene).getEdges();
            List<Edge> diseaseEdges = diseaseNetwork.get(gene).getEdges();
            Map<String, Node> cNet = NetDecoderUtils.createMotifImpactScore(controlEdges);
            Map<String, Node> dNet = NetDecoderUtils.createMotifImpactScore(diseaseEdges);
            (new NetworkFlow()).saveSubNet(cNet, path2create + filename + "_" + gene + "_IP_Control.txt");
            (new NetworkFlow()).saveSubNet(dNet, path2create + filename + "_" + gene + "_IP_Disease.txt");

            Map<String, Double> controlDiff = getFlowDifference(flowDifference, cNet.keySet());
            Map<String, Double> diseaseDiff = getFlowDifference(flowDifference, dNet.keySet());
            Map<String, Double> controlTotal = getTotalFlowInProteins(totalFlowsControl, cNet.keySet());
            Map<String, Double> diseaseTotal = getTotalFlowInProteins(totalFlowsDisease, dNet.keySet());
            Map<String, Double> controlEdgesFlow = getEdgeFlow(cNet);
            Map<String, Double> diseaseEdgesFlow = getEdgeFlow(dNet);
            NetDecoderUtils.saveFlows(controlTotal, path2create + filename + "_" + gene + "_IP_totalFlow_Control.txt");
            NetDecoderUtils.saveFlows(diseaseTotal, path2create + filename + "_" + gene + "_IP_totalFlow_Disease.txt");
            NetDecoderUtils.saveFlows(controlDiff, path2create + filename + "_" + gene + "_IP_flowDifference_Control.txt");
            NetDecoderUtils.saveFlows(diseaseDiff, path2create + filename + "_" + gene + "_IP_flowDifference_Disease.txt");
            NetDecoderUtils.saveFlows(controlEdgesFlow, path2create + filename + "_" + gene + "_IP_edge_flows_Control.txt");
            NetDecoderUtils.saveFlows(diseaseEdgesFlow, path2create + filename + "_" + gene + "_IP_edge_flows_Disease.txt");
            
            /*rJava.exportGML2(path2create + filename + "_" + gene + "_IP_Control",
                    path2create + filename + "_" + gene + "_IP_totalFlow_Control", 
                    path2create + filename + "_" + gene + "_IP_flowDifference_Control");
            
            rJava.exportGML2(path2create + filename + "_" + gene + "_IP_Disease", 
                    path2create + filename + "_" + gene + "_IP_totalFlow_Disease", 
                    path2create + filename + "_" + gene +  "_IP_flowDifference_Disease");
            
            rJava.plotDistribution(path2create + filename + "_" + gene + "_IP_edge_flows_Control", diseaseStates.get(0));
            rJava.plotDistribution(path2create + filename + "_" + gene + "_IP_edge_flows_Disease", diseaseStates.get(1));*/
            String gmlControl = rJava.createScriptExportGML2(path2create + filename + "_" + gene + "_IP_Control",
                    path2create + filename + "_" + gene + "_IP_totalFlow_Control", 
                    path2create + filename + "_" + gene + "_IP_flowDifference_Control");
            
            String gmlDisease = rJava.createScriptExportGML2(path2create + filename + "_" + gene + "_IP_Disease", 
                    path2create + filename + "_" + gene + "_IP_totalFlow_Disease", 
                    path2create + filename + "_" + gene +  "_IP_flowDifference_Disease");
            
            //String distrControl = rJava.createDistributionScript(path2create + filename + "_" + gene + "_IP_edge_flows_Control", diseaseStates.get(0));
            //String distrDisease = rJava.createDistributionScript(path2create + filename + "_" + gene + "_IP_edge_flows_Disease", diseaseStates.get(1));
            
            String distrControl = rJava.createDistributionScript(path2create + filename + "_" + gene + "_IP_edge_flows_Control", control);
            String distrDisease = rJava.createDistributionScript(path2create + filename + "_" + gene + "_IP_edge_flows_Disease", condition);
            
            Rscripts.add(gmlControl);
            Rscripts.add(gmlDisease);
            Rscripts.add(distrControl);
            Rscripts.add(distrDisease);
        }
        
        NetDecoderUtils.saveCCS(topGenesScores, condition,
         filename + "_IMPACT_SCORE_" + condition + ".txt");
    
        String plotIP = rJava.createScritpHeatmap_CCS_1(dir, filename + "_IMPACT_SCORE_" + condition , condition);
        Rscripts.add(plotIP);
                
        String scriptName = dir+filename+"_IMPACT_SCORE_combinedScript_"+condition+".R";
        NetDecoderUtils.combineFiles(Rscripts, scriptName);
        try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        
        //NetDecoderUtils.saveCCS(topGenesScores, diseaseStates.get(1), realRandom, 
        // filename + "_IMPACT_SCORE_" + diseaseStates.get(1) + ".txt");
        
        //rJava.plotVennImpactScore(dir, geneListFile, filename + "_" + diseaseStates.get(1),
        //        diseaseStates.get(1), filename + "_" + diseaseStates.get(1));
       
    }

    public int correlationChange(String gene,
            Map<String, Node> controlNetwork,
            Map<String, Node> diseaseNetwork) {
        
        int count = 1;
        if (controlNetwork.containsKey(gene) && diseaseNetwork.containsKey(gene)) {
            Node geneControl = controlNetwork.get(gene);
            Node geneDisease = diseaseNetwork.get(gene);
            
            List<Edge> edgesControl = geneControl.getEdges();
            List<Edge> edgesDisease = geneDisease.getEdges();
            
            List<Edge> aux = new ArrayList(edgesControl);
            aux.retainAll(edgesDisease);
            for(Edge e:aux){
                int iControl = edgesControl.indexOf(e);
                int iDisease = edgesDisease.indexOf(e);
                Double sigControl = Math.signum(edgesControl.get(iControl).getSignScore());
                Double sigDisease = Math.signum(edgesDisease.get(iDisease).getSignScore());
                if(!sigControl.equals(sigDisease)){
                    count++;
                    //System.out.println(e + "\t" + edgesControl.get(iControl).getSignScore());
                    //System.out.println(e + "\t" + edgesDisease.get(iDisease).getSignScore());
                }
            }
        }
        return count;
    }
    
    public double rewiredInteractions(String gene,
            Map<String, Node> controlNetwork,
            Map<String, Node> diseaseNetwork) {

        Set<Node> inflowControl = new LinkedHashSet();
        Set<Node> inflowDisease = new LinkedHashSet();
        double total = 0.0;
        if (controlNetwork.containsKey(gene) && diseaseNetwork.containsKey(gene)) {
            Node geneControl = controlNetwork.get(gene);
            Node geneDisease = diseaseNetwork.get(gene);
            for (Edge e : geneControl.getEdges()) {
                if (e.getTo().getName().equals(geneControl.getName())) {
                    //System.out.println(e.getTo() + "\t" + e);
                    inflowControl.add(e.getFrom());
                }
            }
            for (Edge e : geneDisease.getEdges()) {
                if (e.getTo().getName().equals(geneDisease.getName())) {
                    //System.out.println(e.getTo() + "\t" + e);
                    inflowDisease.add(e.getFrom());
                }
            }
            //System.out.println(inflowControl);
            //System.out.println(inflowDisease);
            total = inflowControl.size() + inflowDisease.size();
            inflowDisease.removeAll(inflowControl);
        }
        if(inflowDisease.isEmpty()){
            return 1;
        }else{
            return inflowDisease.size() / total;
        }
    }
    
    public void FOXM1() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String dir = cmd.getOptionValue("out");
        //String d = cmd.getOptionValue("d");
        String filename = cmd.getOptionValue("f");
        String ncp = cmd.getOptionValue("ncp");
        String ndp = cmd.getOptionValue("ndp");
        String control = cmd.getOptionValue("control");
        String condition = cmd.getOptionValue("condition");
        RJava rJava = new RJava();
        
        List<String> controlNetworkPaths = Serialization.deserialize(ncp, List.class);
        List<String> diseaseNetworkPaths = Serialization.deserialize(ndp, List.class);
        Map<String, Node> controlNetwork = createNetworkFromPaths(controlNetworkPaths);
        Map<String, Node> diseaseNetwork = createNetworkFromPaths(diseaseNetworkPaths);
        List<String> genes  = new ArrayList();
        genes.add("FOXM1");
        
        List<Map<String, Node>> subnetsControl = NetDecoderUtils.getPathsFromGenes2(controlNetwork, controlNetworkPaths, genes);
        List<Map<String, Node>> subnetsDisease = NetDecoderUtils.getPathsFromGenes2(diseaseNetwork, diseaseNetworkPaths, genes);
        
        (new NetworkFlow()).saveSubNet(subnetsControl.get(0), filename+"_FOXM1_Control.txt");
        (new NetworkFlow()).saveSubNet(subnetsDisease.get(0), filename+"_FOXM1_Disease.txt");
        
        /*String gmlControl = rJava.createScriptExportGML(filename+"_FOXM1_Control", filename + "_FOXM1_totalFlow_Control", 
                filename + "_FOXM1_flowDifference_Control");
        String gmlDisease = rJava.createScriptExportGML(filename+"_FOXM1_Disease", filename + "_FOXM1_totalFlow_Disease", 
                filename + "_FOXM1_flowDifference_Disease");
        
        List<String> Rscripts = new ArrayList();
        
        Rscripts.add(gmlControl);
        Rscripts.add(gmlDisease);
        
        String scriptName = filename + "_FOXM1_combinedScript" + ".R";
        NetDecoderUtils.combineFiles(Rscripts, scriptName);
        try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
            System.out.println("HERE!!");
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }*/
    }
    
    
    public void edgeAnalysis() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String dir = cmd.getOptionValue("out");
        //String d = cmd.getOptionValue("d");
        String filename = cmd.getOptionValue("f");
        String ncp = cmd.getOptionValue("ncp");
        String ndp = cmd.getOptionValue("ndp");
        String control = cmd.getOptionValue("control");
        String condition = cmd.getOptionValue("condition");
        Double corThreshold = Double.valueOf(cmd.getOptionValue("corThreshold"));
        Double ratioThreshold = Double.valueOf(cmd.getOptionValue("ratioThreshold"));
        Integer top = Integer.valueOf(cmd.getOptionValue("top"));
        RJava rJava = new RJava();
        
        String geneListFile = cmd.getOptionValue("g");
        Set<String> genesPaths = null;
        if(!geneListFile.equals("none")){
            genesPaths = new LinkedHashSet(NetworkFlow.getGenes(geneListFile));
        }
        List<String> controlNetworkPaths = Serialization.deserialize(ncp, List.class);
        List<String> diseaseNetworkPaths = Serialization.deserialize(ndp, List.class);
        Map<String, Node> controlNetwork = createNetworkFromPaths(controlNetworkPaths);
        Map<String, Node> diseaseNetwork = createNetworkFromPaths(diseaseNetworkPaths);
        
        Map<String, Map<String, Double>> flowInNetworks = getFlowInNetworks(controlNetwork, diseaseNetwork);
        Map<String, Double> totalFlowsDisease = NetDecoderUtils.getTotalFlow(diseaseNetwork);
        Map<String, Double> totalFlowsControl = NetDecoderUtils.getTotalFlow(controlNetwork);
        
        Set<Edge> cEdges = getAllEdges(controlNetwork);
        Set<Edge> dEdges = getAllEdges(diseaseNetwork);
        Map<String, Map<String, Double>> flowMatrix = new LinkedHashMap();
        
        if(!cmd.hasOption("overlap")){ //use only edges in both networks to infer key edges?
            dEdges.addAll(cEdges);
        }else{
            dEdges.retainAll(cEdges);
        }
        Map<String, Double> xxControl = new LinkedHashMap();
        Map<String, Double> xxDisease = new LinkedHashMap();
        for(Edge e:dEdges){
            Edge eControl = NetDecoderUtils.getEdge(controlNetwork, e);
            Edge eDisease = NetDecoderUtils.getEdge(diseaseNetwork, e);
            //System.out.println(eDisease.getFlow() + "\t" + eDisease.getSignScores());
            if(eControl != null && eDisease != null){
                xxControl.put(e.toString(), eControl.getFlow());
                xxDisease.put(e.toString(), eDisease.getFlow());
            }else if(eControl != null){
                xxControl.put(e.toString(), eControl.getFlow());
                xxDisease.put(e.toString(), 0.0);
            }else if(eDisease != null){
                xxControl.put(e.toString(), 0.0);
                xxDisease.put(e.toString(), eDisease.getFlow());
            }
        }
        flowMatrix.put(control, xxControl);
        flowMatrix.put(condition, xxDisease);
        
        //creating folders...
        String path2create = dir + condition + "/";
        new File(path2create).mkdirs();
        
        Map<String, Map<String, Double>> aux = changeMapping(flowMatrix);
        String name = path2create + filename+"_flowMatrix";
        saveFlowMatrix(aux, control, condition, name+".txt");
        
        List<String> Rscripts = new ArrayList();
        //rJava.plotBarplot(path2create, name, condition, corThreshold, ratioThreshold, filename);
        String barplot = rJava.createBarplotScript(path2create, name, condition, corThreshold, ratioThreshold, filename);
        //Rscripts.add(barplot);
 
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        //Load key edges flow matrix for differential edge-centered network enrichment
        String keyEdgesFile = path2create + filename + "_keyEdges.txt";
        System.out.println(keyEdgesFile);
        Set<Edge> keyEdges = loadKeyEdges(keyEdgesFile);
        
        //==========save full networks
        NetDecoderUtils.savePaths(NetDecoderUtils.convertString2Paths(diseaseNetworkPaths), condition + "/" + filename + "_ALL_paths_disease.txt");
        NetDecoderUtils.savePaths(NetDecoderUtils.convertString2Paths(controlNetworkPaths), condition + "/" + filename + "_ALL_paths_control.txt");
        Map<String, Double> controlDFNdiff = getFlowDifference(flowInNetworks, controlNetwork.keySet());
        Map<String, Double> diseaseDFNdiff = getFlowDifference(flowInNetworks, diseaseNetwork.keySet());
        Map<String, Double> controlDFNtotal = getTotalFlowInProteins(totalFlowsControl, controlNetwork.keySet());
        Map<String, Double> diseaseDFNtotal = getTotalFlowInProteins(totalFlowsDisease, diseaseNetwork.keySet());
        NetDecoderUtils.saveFlows(controlDFNtotal, dir + "/" + condition + "/" + filename + "_FULL_totalFlow_Control.txt");
        NetDecoderUtils.saveFlows(diseaseDFNtotal, condition + "/" +filename + "_FULL_totalFlow_Disease.txt");
        NetDecoderUtils.saveFlows(controlDFNdiff, condition + "/" +filename + "_FULL_flowDifference_Control.txt");
        NetDecoderUtils.saveFlows(diseaseDFNdiff, condition + "/" +filename + "_FULL_flowDifference_Disease.txt");
        NetDecoderUtils.saveGeneList(controlNetwork, condition + "/" +filename + "_FULL_GENES_Control.txt");
        NetDecoderUtils.saveGeneList(diseaseNetwork, condition + "/" +filename + "_FULL_GENES_Disease.txt");
        (new NetworkFlow()).saveSubNet(controlNetwork, condition + "/" +filename+"_FULL_Control.txt");
        (new NetworkFlow()).saveSubNet(diseaseNetwork, condition + "/" +filename+"_FULL_Disease.txt");
        
        //rJava.exportGML(condition + "/" +filename+"_FULL_Control", condition + "/" +filename + "_FULL_totalFlow_Control", 
        //        condition + "/" +filename + "_FULL_flowDifference_Control");
        //rJava.exportGML(condition + "/" +filename+"_FULL_Disease", condition + "/" +filename + "_FULL_totalFlow_Disease", 
        //        condition + "/" + filename + "_FULL_flowDifference_Disease");
        
        String gmlControl = rJava.createScriptExportGML(condition + "/" +filename+"_FULL_Control", condition + "/" +filename + "_FULL_totalFlow_Control", 
                condition + "/" +filename + "_FULL_flowDifference_Control");
        String gmlDisease = rJava.createScriptExportGML(condition + "/" +filename+"_FULL_Disease", condition + "/" +filename + "_FULL_totalFlow_Disease", 
                condition + "/" + filename + "_FULL_flowDifference_Disease");
        Rscripts.add(gmlControl);
        Rscripts.add(gmlDisease);
        //===========
        //Now have to extract paths containing key edges
        Map<Edge, List<Path>> diseaseEdges2Path = NetDecoderUtils.getPathsFromEdges(diseaseNetworkPaths, keyEdges);
        Map<Edge, List<Path>> controlEdges2Path = NetDecoderUtils.getPathsFromEdges(controlNetworkPaths, keyEdges);

        //String name_2 = path2create + filename + "_" + control + "_" + condition;
        String name_2 = path2create + filename;
        NetDecoderUtils.comparePaths(diseaseEdges2Path, controlEdges2Path,
                totalFlowsDisease, totalFlowsControl, flowInNetworks,
                rJava, condition, genesPaths, name_2, path2create, Rscripts);
        
        System.out.println("Saving key targets");
        Set<String> sinks = prioritizeSinks(controlNetwork, diseaseNetwork, condition, top, rJava, name_2, Rscripts);
        System.out.println("Saving Network routers");
        Set<String> hidden = prioritizeHiddenProteins(controlNetwork, diseaseNetwork, condition, top, rJava, name_2, Rscripts);
        Set<String> IPgenes = computeImpactScore(controlNetwork, diseaseNetwork).keySet();
        prioritizePaths(controlNetworkPaths, diseaseNetworkPaths, totalFlowsDisease, flowInNetworks,
                sinks, hidden, IPgenes, rJava, name_2, Rscripts);
        
        String scriptName = filename + "_NETWORK_ROUTERS_combinedScript" + ".R";
        NetDecoderUtils.combineFiles(Rscripts, scriptName);
        try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        //compute and ranking genes by impact score.
        impactScore(path2create);
    }
    
    public Map<String, Map<String, Double>> getFlowInNetworks(
            Map<String, Node> controlNetwork,
            Map<String, Node> diseaseNetwork){
    
        Set<String> allGenes = new LinkedHashSet(diseaseNetwork.keySet());
        allGenes.addAll(controlNetwork.keySet());
        Map<String, Map<String, Double>> flows = new LinkedHashMap();
        
        for(String gene:allGenes){
            Map<String, Double> tmp = new LinkedHashMap();
            if(diseaseNetwork.containsKey(gene) && controlNetwork.containsKey(gene)){
                Double difference = diseaseNetwork.get(gene).getTotalFlow() - controlNetwork.get(gene).getTotalFlow();
                tmp.put("control", controlNetwork.get(gene).getTotalFlow());
                tmp.put("disease", diseaseNetwork.get(gene).getTotalFlow());
                tmp.put("difference", difference);
            }else if(diseaseNetwork.containsKey(gene)){
                tmp.put("control", 0.0);
                tmp.put("disease", diseaseNetwork.get(gene).getTotalFlow());
                tmp.put("difference", diseaseNetwork.get(gene).getTotalFlow());
            }else{
                tmp.put("control", controlNetwork.get(gene).getTotalFlow());
                tmp.put("disease", 0.0);
                tmp.put("difference", controlNetwork.get(gene).getTotalFlow());
            }
            flows.put(gene, tmp);
        }
        
        return flows;
    }
    
    public Set<String> prioritizeSinks(
            Map<String, Node> controlNetwork,
            Map<String, Node> diseaseNetwork,
            String condition,
            int topSinks,
            RJava rJava,
            String filename,
            List<String> Rscripts) throws IOException{
        
        Map<String, Map<String, Double>> flowInNetworks = getFlowInNetworks(controlNetwork, diseaseNetwork);
        Map<String, Double> controlSinksMap = NetDecoderUtils.getSinks(controlNetwork);
        Map<String, Double> diseaseSinksMap = NetDecoderUtils.getSinks(diseaseNetwork);
        Set<String> allSinks = new LinkedHashSet(controlSinksMap.keySet());
        allSinks.addAll(diseaseSinksMap.keySet());
        Map<String, Double> flowDifference = NetDecoderUtils.getFlowDifference(flowInNetworks, allSinks);
        
        Map<String, Double> sortedScores = NetworkFlow.sortByValues(flowDifference);
        List<String> aux = new ArrayList(sortedScores.keySet());
        List<String> topGenes_down = aux.subList(0, topSinks);
        List<String> topGenes_up = aux.subList(aux.size() - topSinks, aux.size());
        List<String> topGenes = new ArrayList(topGenes_down);
        topGenes.addAll(topGenes_up);
        Map<String, Double> topGenesScores = new LinkedHashMap();
        for(String g:topGenes){
            if(sortedScores.get(g) > 0){
                topGenesScores.put(g, Math.log10(sortedScores.get(g)));
            }else{
                double tmp = -Math.log10(Math.abs(sortedScores.get(g)));
                topGenesScores.put(g, tmp);
            }
        }
        String name = filename + "_SINKS";
        NetDecoderUtils.saveFlows(topGenesScores, name + ".txt");
        //rJava.plotHeatmapSH2(name, condition, topSinks, "Key targets",filename);
        String heatmap = rJava.createHeatmapSH2Script(name, condition, topSinks, "Key targets",filename);
        Rscripts.add(heatmap);
        //save network motifs for network routers here (downstream genes, network router is "from" in the edge): 
        Map<String, Double> totalFlowsDisease = NetDecoderUtils.getTotalFlow(diseaseNetwork);
        Map<String, Double> totalFlowsControl = NetDecoderUtils.getTotalFlow(controlNetwork);
        for (String gene : topGenesScores.keySet()) {
            if (controlNetwork.containsKey(gene)) {
                List<Edge> controlEdges = controlNetwork.get(gene).getEdges();
                Map<String, Node> cNet = NetDecoderUtils.createMotifTargets(gene, controlEdges);
                (new NetworkFlow()).saveSubNet(cNet, filename + "_" + gene + "_KT_Control.txt");
                
                Map<String, Double> controlDiff = getFlowDifference(flowInNetworks, cNet.keySet());
                Map<String, Double> controlTotal = getTotalFlowInProteins(totalFlowsControl, cNet.keySet());
                Map<String, Double> controlEdgesFlow = getEdgeFlow(cNet);
                NetDecoderUtils.saveFlows(controlTotal, filename + "_" + gene + "_KT_totalFlow_Control.txt");
                NetDecoderUtils.saveFlows(controlDiff, filename + "_" + gene + "_KT_flowDifference_Control.txt");
                NetDecoderUtils.saveFlows(controlEdgesFlow, filename + "_" + gene + "_KT_edge_flows_Control.txt");
                
                //rJava.exportGML2(filename + "_" + gene + "_KT_Control",
                //        filename + "_" + gene + "_KT_totalFlow_Control",
                //        filename + "_" + gene + "_KT_flowDifference_Control");
                String gmlControl = rJava.createScriptExportGML2(filename + "_" + gene + "_KT_Control",
                        filename + "_" + gene + "_KT_totalFlow_Control",
                        filename + "_" + gene + "_KT_flowDifference_Control");

                //rJava.plotDistribution(filename + "_" + gene + "_KT_edge_flows_Control", "Control");
                String distrControl = rJava.createDistributionScript(filename + "_" + gene + "_KT_edge_flows_Control", "Control");
                Rscripts.add(gmlControl);
                Rscripts.add(distrControl);
            }
            if (diseaseNetwork.containsKey(gene)) {
                List<Edge> diseaseEdges = diseaseNetwork.get(gene).getEdges();
                Map<String, Node> dNet = NetDecoderUtils.createMotifTargets(gene, diseaseEdges);
                (new NetworkFlow()).saveSubNet(dNet, filename + "_" + gene + "_KT_Disease.txt");

                Map<String, Double> diseaseDiff = getFlowDifference(flowInNetworks, dNet.keySet());
                Map<String, Double> diseaseTotal = getTotalFlowInProteins(totalFlowsDisease, dNet.keySet());
                Map<String, Double> diseaseEdgesFlow = getEdgeFlow(dNet);
                NetDecoderUtils.saveFlows(diseaseTotal, filename + "_" + gene + "_KT_totalFlow_Disease.txt");
                NetDecoderUtils.saveFlows(diseaseDiff, filename + "_" + gene + "_KT_flowDifference_Disease.txt");
                NetDecoderUtils.saveFlows(diseaseEdgesFlow, filename + "_" + gene + "_KT_edge_flows_Disease.txt");

                /*rJava.exportGML2(filename + "_" + gene + "_KT_Disease",
                        filename + "_" + gene + "_KT_totalFlow_Disease",
                        filename + "_" + gene + "_KT_flowDifference_Disease");

                rJava.plotDistribution(filename + "_" + gene + "_KT_edge_flows_Disease", condition);*/
                
                String gmlDisease = rJava.createScriptExportGML2(filename + "_" + gene + "_KT_Disease",
                        filename + "_" + gene + "_KT_totalFlow_Disease",
                        filename + "_" + gene + "_KT_flowDifference_Disease");

                String distrDisease = rJava.createDistributionScript(filename + "_" + gene + "_KT_edge_flows_Disease", condition);
                Rscripts.add(gmlDisease);
                Rscripts.add(distrDisease);
            }
        }
        
        return topGenesScores.keySet();
    }
    
    public Set<String> prioritizeHiddenProteins(
            Map<String, Node> controlNetwork,
            Map<String, Node> diseaseNetwork,
            String condition,
            int topHidden,
            RJava rJava,
            String filename,
            List<String> Rscripts) throws IOException{
        
        Map<String, Map<String, Double>> flowInNetworks = getFlowInNetworks(controlNetwork, diseaseNetwork);
        Map<String, Double> hiddenProteinsControl = NetDecoderUtils.getHiddenProteins(controlNetwork);
        Map<String, Double> hiddenProteinsDisease = NetDecoderUtils.getHiddenProteins(diseaseNetwork);
        Set<String> allHidden = new LinkedHashSet(hiddenProteinsControl.keySet());
        allHidden.addAll(hiddenProteinsDisease.keySet());
        Map<String, Double> flowDifference = NetDecoderUtils.getFlowDifference(flowInNetworks, allHidden);
        
        Map<String, Double> sortedScores = NetworkFlow.sortByValues(flowDifference);
        List<String> aux = new ArrayList(sortedScores.keySet());
        List<String> topGenes_down = aux.subList(0, topHidden);
        List<String> topGenes_up = aux.subList(aux.size() - topHidden, aux.size());
        List<String> topGenes = new ArrayList(topGenes_down);
        topGenes.addAll(topGenes_up);
        Map<String, Double> topGenesScores = new LinkedHashMap();
        for(String g:topGenes){
            if(sortedScores.get(g) > 0){
                topGenesScores.put(g, Math.log10(sortedScores.get(g)));
            }else{
                double tmp = -Math.log10(Math.abs(sortedScores.get(g)));
                topGenesScores.put(g, tmp);
            }
        }
        String name = filename + "_HIDDEN";
        NetDecoderUtils.saveFlows(topGenesScores, name + ".txt");
        //rJava.plotHeatmapSH2(name, condition, topHidden, "Network routers", filename);
        String heatmap = rJava.createHeatmapSH2Script(name, condition, topHidden, "Network routers", filename);
        Rscripts.add(heatmap);
        //save network motifs for network routers here (downstream genes, network router is "from" in the edge): 
        Map<String, Double> totalFlowsDisease = NetDecoderUtils.getTotalFlow(diseaseNetwork);
        Map<String, Double> totalFlowsControl = NetDecoderUtils.getTotalFlow(controlNetwork);
        for (String gene : topGenesScores.keySet()) {
            if (controlNetwork.containsKey(gene)) {
                List<Edge> controlEdges = controlNetwork.get(gene).getEdges();
                Map<String, Node> cNet = NetDecoderUtils.createMotifNetworkRouters(gene, controlEdges);
                (new NetworkFlow()).saveSubNet(cNet, filename + "_" + gene + "_NR_Control.txt");
                
                Map<String, Double> controlDiff = getFlowDifference(flowInNetworks, cNet.keySet());
                Map<String, Double> controlTotal = getTotalFlowInProteins(totalFlowsControl, cNet.keySet());
                Map<String, Double> controlEdgesFlow = getEdgeFlow(cNet);
                NetDecoderUtils.saveFlows(controlTotal, filename + "_" + gene + "_NR_totalFlow_Control.txt");
                NetDecoderUtils.saveFlows(controlDiff, filename + "_" + gene + "_NR_flowDifference_Control.txt");
                NetDecoderUtils.saveFlows(controlEdgesFlow, filename + "_" + gene + "_NR_edge_flows_Control.txt");
                
                /*rJava.exportGML2(filename + "_" + gene + "_NR_Control",
                        filename + "_" + gene + "_NR_totalFlow_Control",
                        filename + "_" + gene + "_NR_flowDifference_Control");

                rJava.plotDistribution(filename + "_" + gene + "_NR_edge_flows_Control", "Control");*/
                String gmlControl = rJava.createScriptExportGML2(filename + "_" + gene + "_NR_Control",
                        filename + "_" + gene + "_NR_totalFlow_Control",
                        filename + "_" + gene + "_NR_flowDifference_Control");

                String distrControl = rJava.createDistributionScript(filename + "_" + gene + "_NR_edge_flows_Control", "Control");
                Rscripts.add(gmlControl);
                Rscripts.add(distrControl);
            }
            if (diseaseNetwork.containsKey(gene)) {
                List<Edge> diseaseEdges = diseaseNetwork.get(gene).getEdges();
                Map<String, Node> dNet = NetDecoderUtils.createMotifNetworkRouters(gene, diseaseEdges);
                (new NetworkFlow()).saveSubNet(dNet, filename + "_" + gene + "_NR_Disease.txt");

                Map<String, Double> diseaseDiff = getFlowDifference(flowInNetworks, dNet.keySet());
                Map<String, Double> diseaseTotal = getTotalFlowInProteins(totalFlowsDisease, dNet.keySet());
                Map<String, Double> diseaseEdgesFlow = getEdgeFlow(dNet);
                NetDecoderUtils.saveFlows(diseaseTotal, filename + "_" + gene + "_NR_totalFlow_Disease.txt");
                NetDecoderUtils.saveFlows(diseaseDiff, filename + "_" + gene + "_NR_flowDifference_Disease.txt");
                NetDecoderUtils.saveFlows(diseaseEdgesFlow, filename + "_" + gene + "_NR_edge_flows_Disease.txt");
            
                String gmlDisease = rJava.createScriptExportGML2(filename + "_" + gene + "_NR_Disease",
                        filename + "_" + gene + "_NR_totalFlow_Disease",
                        filename + "_" + gene + "_NR_flowDifference_Disease");

                String distrDisease = rJava.createDistributionScript(filename + "_" + gene + "_NR_edge_flows_Disease", condition);
                
                Rscripts.add(gmlDisease);
                Rscripts.add(distrDisease);
            }
        }
        return topGenesScores.keySet();
    }
    
    /*public Set<String> prioritizeSinks(
            List<String> controlPaths,
            List<String> diseasePaths,
            String condition,
            double ratioThreshold,
            int topSinks,
            RJava rJava,
            String filename){
        //optimize the code to select transcription factors?
        Map<String, Node> controlPaths = createNetworkFromPaths(new ArrayList(controlPaths));
        Map<String, Node> diseasePaths = createNetworkFromPaths(new ArrayList(diseasePaths));
        Map<String, Map<String, Double>> data = new LinkedHashMap();
        Map<String, Double> controlSinksMap = NetDecoderUtils.getSinks(controlPaths);
        Map<String, Double> diseaseSinksMap = NetDecoderUtils.getSinks(diseasePaths);
        for (String n : diseaseSinksMap.keySet()) {
            Map<String, Double> auxData = new LinkedHashMap();
            if (controlSinksMap.containsKey(n)) {
                Double ratio = diseaseSinksMap.get(n) / controlSinksMap.get(n);
                if (ratio > ratioThreshold) { //with 2 includes the path_dir BCL2 as a regulator...
                    auxData.put("control", controlSinksMap.get(n));
                    auxData.put("disease", diseaseSinksMap.get(n));
                    data.put(n, auxData);
                }
            } else {
                auxData.put("control", 0.0);
                auxData.put("disease", diseaseSinksMap.get(n));
                data.put(n, auxData);
            }
        }
        Map<String, Map<String, Double>> rData = changeMapping(data);
        Map<String, Double> sortedDisease = NetworkFlow.sortByValues(rData.get("disease"));
        String name = filename + "_SINKS";
        NetDecoderUtils.saveData(data, name+".txt");
        rJava.plotHeatmapSH(name, condition, topSinks, "Sinks",filename);
        
        List<String> genes = new ArrayList(sortedDisease.keySet());
        Set<String> topGenes = new LinkedHashSet(genes.subList(genes.size() - topSinks, genes.size()));
        
        return topGenes;
    }*/
    
    /*public Set<String> prioritizeHiddenProteins(
            List<String> controlPaths,
            List<String> diseasePaths,
            String condition,
            double ratioThreshold,
            int topHidden,
            RJava rJava,
            String filename){
        
        Map<String, Node> controlPaths = createNetworkFromPaths(new ArrayList(controlPaths));
        Map<String, Node> diseasePaths = createNetworkFromPaths(new ArrayList(diseasePaths));
        Map<String, Map<String, Double>> data = new LinkedHashMap();
        Map<String, Double> hiddenProteinsControl = NetDecoderUtils.getHiddenProteins(controlPaths);
        Map<String, Double> hiddenProteinsDisease = NetDecoderUtils.getHiddenProteins(diseasePaths);
        for(String p:hiddenProteinsDisease.keySet()){
            Map<String, Double> auxData = new LinkedHashMap();
            if (hiddenProteinsControl.containsKey(p)) {
                double ratio = hiddenProteinsDisease.get(p) / hiddenProteinsControl.get(p);
                if (ratio > ratioThreshold) { //10
                    auxData.put("control", hiddenProteinsControl.get(p));
                    auxData.put("disease", hiddenProteinsDisease.get(p));
                    data.put(p, auxData);
                }
            } else {
                auxData.put("control", 0.0);
                auxData.put("disease", hiddenProteinsDisease.get(p));
                data.put(p, auxData);
            }
        }
        Map<String, Map<String, Double>> rData = changeMapping(data);
        Map<String, Double> sortedDisease = NetworkFlow.sortByValues(rData.get("disease"));
        String name = filename + "_HIDDEN";
        NetDecoderUtils.saveData(data, name+".txt");
        rJava.plotHeatmapSH(name, condition, topHidden, "Intermediary",filename);
        
        List<String> genes = new ArrayList(sortedDisease.keySet());
        Set<String> topGenes = new LinkedHashSet(genes.subList(genes.size() - topHidden, genes.size()));
        
        return topGenes;
    }*/
    
    public void prioritizePaths(
            List<String> controlPaths,
            List<String> diseasePaths,
            Map<String, Double> totalFlows,
            Map<String, Map<String, Double>> flowDifference,
            Set<String> sinks,
            Set<String> hidden,
            Set<String> IPgenes,
            RJava rJava,
            String filename,
            List<String> Rscripts) throws IOException {
        
        //get all paths containing sinks with fold change > 2
        sinks.removeAll(IPgenes);
        hidden.removeAll(IPgenes);
        Set<Path> paths = new LinkedHashSet();
        for(String p:diseasePaths){
            Path path = NetDecoderUtils.convertString2Path(p);
            boolean flag=false;
            for(Edge e:path.getPath()){
                Node from  = e.getFrom();
                Node to = e.getTo(); //is it a key target or a network router?
                if (sinks.contains(to.getName()) && (hidden.contains(from.getName()) || hidden.contains(to.getName()))) {
                    flag = true;
                    break; //is it a key target or a path_dir with high impact score?
                } else if(sinks.contains(to.getName()) && (IPgenes.contains(from.getName()) || IPgenes.contains(to.getName()))){
                    flag = true;
                    break; //is it a network router or path_dir with high impact score?
                } else if((hidden.contains(from.getName()) || hidden.contains(to.getName())) &&
                        (IPgenes.contains(from.getName()) || IPgenes.contains(to.getName()))){
                    flag = true;
                    break;
                }
            }
            if(flag){
                paths.add(path);
                //System.out.println(path_dir);
            }
        }
        List<String> tmpPaths = NetDecoderUtils.convertPaths2String(new ArrayList(paths));
        Map<String, Node> network = NetDecoderUtils.createNetworkFromPaths2Plot(tmpPaths);
        Map<String, Double> flowDiff = getFlowDifference(flowDifference, network.keySet());
        Map<String, Double> flowTotal = getTotalFlowInProteins(totalFlows, network.keySet());
        
        if (!network.isEmpty()) {
            (new NetworkFlow()).saveSubNet(network, filename + "_PRIORITIZED_NETWORK.txt");

            NetDecoderUtils.saveFlows(flowDiff,
                    filename + "_flowDifference_PRIORITIZED_NETWORK.txt");
            NetDecoderUtils.saveFlows(flowTotal,
                    filename + "_flowTotal_PRIORITIZED_NETWORK.txt");

            //rJava.exportGML(filename + "_PRIORITIZED_NETWORK",
            //        filename + "_flowTotal_PRIORITIZED_NETWORK",
            //        filename + "_flowDifference_PRIORITIZED_NETWORK");
            String gml1 = rJava.createScriptExportGML(filename + "_PRIORITIZED_NETWORK",
                    filename + "_flowTotal_PRIORITIZED_NETWORK",
                    filename + "_flowDifference_PRIORITIZED_NETWORK");
            Rscripts.add(gml1);
        } else {
            System.out.println("No paths fulfill the criteria to be be prioritized!");
        }
    }
    
    public void prioritizePaths(
            List<String> controlPaths,
            List<String> diseasePaths,
            Map<Edge, List<Path>> diseaseEdges2Path,
            Map<Edge, List<Path>> controlEdges2Path,
            Map<String, Double> totalFlows,
            Map<String, Map<String, Double>> flowDifference,
            Set<String> sinks,
            Set<String> hidden,
            boolean includeKeyEdges,
            RJava rJava,
            String filename) throws IOException{
        
        //get all paths containing sinks with fold change > 2
        Set<Path> paths = new LinkedHashSet();
        Set<Path> pathsFilteredByEdges = new LinkedHashSet();
        for(String p:diseasePaths){
            Path path = NetDecoderUtils.convertString2Path(p);
            boolean flag=false;
            for(Edge e:path.getPath()){
                Node from  = e.getFrom();
                Node to = e.getTo();
                if(sinks.contains(to.getName())){
                    if(hidden.contains(from.getName()) || hidden.contains(to.getName())){
                        flag=true;
                        break;
                    }
                }
            }
            if(flag){
                paths.add(path);
                //System.out.println(path_dir);
            }
        }
        if (includeKeyEdges) {
            for (Edge key : diseaseEdges2Path.keySet()) {
                for (Path path : paths) {
                    if (path.getPath().contains(key)) {
                        pathsFilteredByEdges.add(path);
                        //System.out.println(key + " " + path_dir);
                    }
                }
            }
        }
        Set<Path> aux = null;
        if(includeKeyEdges){
            //System.out.println(pathsFilteredByEdges);
            aux = pathsFilteredByEdges;
        }else{
            //System.out.println(paths);
            aux = paths;
        }
        
        List<String> tmpPaths = NetDecoderUtils.convertPaths2String(new ArrayList(aux));
        Map<String, Node> network = createNetworkFromPaths(tmpPaths);
        Map<String, Double> flowDiff = getFlowDifference(flowDifference, network.keySet());
        Map<String, Double> flowTotal = getTotalFlowInProteins(totalFlows, network.keySet());
        
        if(!network.isEmpty()){
        (new NetworkFlow()).saveSubNet(network, filename + "_prioritizedNetwork.txt");
        
        NetDecoderUtils.saveFlows(flowDiff,
                filename + "_flowDifference_prioritizedNetwork.txt");
        NetDecoderUtils.saveFlows(flowTotal,
                filename + "_flowTotal_prioritizedNetwork.txt");
        
        rJava.exportGML(filename + "_prioritizedNetwork",
                filename + "_flowTotal_prioritizedNetwork", 
                filename + "_flowDifference_prioritizedNetwork");
        }else{
            System.out.println("No paths fulfill the criteria to be be prioritized!");
        }
    }
    
    public void DFNAnalysis(
            List<String> controlPaths,
            List<String> diseasePaths) throws IOException {
        
        List<Path> diseaseSpecPaths = NetDecoderUtils.getPhenotypeSpecificPaths(diseasePaths, controlPaths);
        List<Path> controlSpecPaths = NetDecoderUtils.getPhenotypeSpecificPaths(controlPaths, diseasePaths);
        
        System.out.println(diseaseSpecPaths.size());
        System.out.println(diseasePaths.size());
        System.out.println(controlSpecPaths.size());
        System.out.println(controlPaths.size());
        List<String> sDiseasePaths = 
                NetDecoderUtils.transformPaths2String(diseaseSpecPaths);
        List<String> sControlPaths = 
                NetDecoderUtils.transformPaths2String(controlSpecPaths);
        
        Map<String, Node> diseaseNetwork = NetDecoderUtils.createNetworkFromPaths(sDiseasePaths);
        Map<String, Node> controlNetwork = NetDecoderUtils.createNetworkFromPaths(sControlPaths);
        System.out.println(diseaseNetwork.size());
        System.out.println(controlNetwork.size());
        Set<String> genesDisease = new LinkedHashSet(diseaseNetwork.keySet());
        Set<String> genesControl = new LinkedHashSet(controlNetwork.keySet());
        genesDisease.removeAll(controlNetwork.keySet());
        genesControl.removeAll(diseaseNetwork.keySet());
        System.out.println("GENES: " + genesDisease.size());
        System.out.println("GENES: " + genesControl.size());
        
        Collections.sort(diseaseSpecPaths, (Path p1, Path p2) -> Double.compare(p1.flow, p2.flow));
        Collections.sort(controlSpecPaths, (Path p1, Path p2) -> Double.compare(p1.flow, p2.flow));
        
        //NetDecoderUtils.savePaths(diseasePaths, dir+filename+"_DFN_paths_disease.txt");
        //NetDecoderUtils.savePaths(controlPaths, dir+filename+"_DFN_paths_control.txt");
        NetDecoderUtils.savePaths(diseaseSpecPaths, "TEST_DFN_paths_disease.txt");
        NetDecoderUtils.savePaths(controlSpecPaths, "TEST_DFN_paths_control.txt");
    }
    
    public static void main(String[] args) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
    	try {
            NetDecoder pn = new NetDecoder();
            if(pn.checkArgs(args)){
                pn.runAnalysis();
            }else{
                System.out.println("ferrou");
            }
    	}
    	catch (Throwable t) {
    		System.err.println("Uncaught exception - " + t.getMessage());
            t.printStackTrace(System.err);
    	}
    }
    
    public List<String> setDefaultSinks(){
        //Use current CellNet TFs for this?
        List<String> TFAnnot = new ArrayList();
        TFAnnot.add("GO:0003676"); //nucleic acid binding
        TFAnnot.add("GO:0006355"); //regulation of transcription, DNA-templated
        TFAnnot.add("GO:0008134"); //transcription factor binding
        //TFAnnot.add("GO:0003677"); //DNA binding
        return TFAnnot;
    }
    
}