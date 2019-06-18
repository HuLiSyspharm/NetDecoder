
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netdecoder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author edroaldo
 */
public class NetworkFlow implements Serializable{
    //private Map<String, List<String>> network;
    //private Map<String, Node> network;
    private Map<String, Integer> symbolTable;
    private final List<NodeInfo> nodes;
    private Map<String, NodeInfo> nodesPath = new LinkedHashMap<String, NodeInfo>();
    private Map<String, Edge> edgeTo;
    private Map<String, Double> distTo;
    private IndexMinPQ<Double> ipq;
    private Node source;
    private Node target;
    private double value;
    private int E;
    private int nodeId;
    private PriorityQueue<Edge> pq;
    private Queue<Edge> mst;
    private double cost;
    private String keys[];
    
    Set<String> enrichment = new LinkedHashSet<String>();   //Proteinas para GO analysis
    LinkedHashSet<Edge> predicted = new LinkedHashSet<Edge>();  //interacoes preditas
    int pathNum = 1;
    Map<Integer, List<Edge>> highScoreProteins = new LinkedHashMap<Integer, List<Edge>>();
    //LinkedHashMap<String, Node> subnet = new LinkedHashMap<String, Node>();
        
    public NetworkFlow(){ 
        //network = new LinkedHashMap<String, List<String>>();
        //network = new LinkedHashMap<String, Node>();
        pq = new PriorityQueue<Edge>();
        nodes = new ArrayList<NodeInfo>();
        value = 0.0;
        E = 0;
        nodeId = 1;
    }
    
    public NetworkFlow(Node source, Node target){
        this.source = source;
        this.target = target;
        nodes = null;
    }
    
    public static void main(String args[]) throws IOException{
        
    }
    
    
    public void saveAllPathsGO(Map<String, Node> network, Map<Integer, List<Edge>> paths, 
            OntologyUtils utils) throws IOException{
        
        for(Integer i:paths.keySet()){
            Set<Edge> path = new LinkedHashSet<Edge>(paths.get(i));
            List<String> goPath = getGOfromPath(network, path);
            String filename = "Path_" + i;
            savePathGO(goPath, path, utils, filename);
        }
    }
    
    public void saveTotalFlowByCategory(Map<String, Node> network,
                                        String comparison,
                                        List<String> category,
                                        String head,
                                        String file) throws IOException{
            File f = new File(file);
            if(!f.exists()){
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("comparison" + "\t" + head + "\t" + "netflow");
            bw.newLine();
            for (String n : category) {
                Node n1 = network.get(n);
                //if(n1.getName().equals("s") || n1.getName().equals("t")) continue;
                bw.write(comparison + "\t" + n1.getSymbol() + "\t" + n1.getTotalFlow());
                bw.newLine();
            }
            bw.close();
    }
    public void saveTotalFlow(Map<String, Node> network, 
                                String head,
                                String file) throws IOException {
            //String file = "/home/edroaldo/Documents/Projects/CRISP/analysis/"+filename+".txt";
            File f = new File(file);
            if(!f.exists()){
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(head + "\t" + "netflow");
            bw.newLine();
            for (String n : network.keySet()) {
                Node n1 = network.get(n);
                //if(n1.getName().equals("s") || n1.getName().equals("t")) continue;
                bw.write(n1.getSymbol() + "\t" + n1.getTotalFlow());
                bw.newLine();
            }
            bw.close();
    }
    
    public static void saveGOscoresW(Map<String, Double> GOscores,
                             String condition,
                             OntologyUtils utils,
                             String file) throws IOException{
        
            File f = new File(file);
            if(!f.exists()){
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("condition" +"\t" + "goid" + "\t" + "goname" + "\t" + "score");
            bw.newLine();
            Map<String, OntologyUtils.Term> gos = utils.getGOHierarchy();
            for (String g : GOscores.keySet()) {
                OntologyUtils.Term term = gos.get(g);
                bw.write(condition + "\t" + term.getId() + "\t" + term.getName() + "\t" + GOscores.get(g));
                bw.newLine();
            }
            
            bw.close();
    
    }
    public static void saveGOscores(Map<String, Integer> GOscores,
                            String condition,
                             OntologyUtils utils,
                             String file) throws IOException{
        
            File f = new File(file);
            if(!f.exists()){
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("condition" +"\t" + "goid" + "\t" + "goname" + "\t" + "score");
            bw.newLine();
            Map<String, OntologyUtils.Term> gos = utils.getGOHierarchy();
            for (String g : GOscores.keySet()) {
                OntologyUtils.Term term = gos.get(g);
                bw.write(condition + "\t" + term.getId() + "\t" + term.getName() + "\t" + GOscores.get(g));
                bw.newLine();
            }
            
            bw.close();
    
    }
    
    public void savePathGO(List<String> go, Set<Edge> path, OntologyUtils utils, String filename) throws IOException{
            String file="/home/edroaldo/Documents/Projects/CRISP/cas9Network/Short-Clean-data-log2/Clean/Cas9-20317/DGE/Fold-Change-P-value/";
            file = file + filename;
            File f = new File(file);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(Edge e:path){
                bw.write(e + "\t");
            }
            bw.write("\n");
            for (String s : go) {
                OntologyUtils.Term term = utils.getGOHierarchy().get(s);
                bw.write(term.getId() + ": " + term.getName() + "\n");
            }
            bw.close();
    }
    
    public Map<Integer, List<Edge>> getWTPaths(Map<Integer, List<Edge>> paths, int numPaths){
        Map<Integer, List<Edge>> wtPaths = new LinkedHashMap<Integer, List<Edge>>();
        Map<Integer, Double> lowCost =  new LinkedHashMap<Integer, Double>();
        for(Integer p:paths.keySet()){
            double cost = 0.0;
            for(Edge e:paths.get(p)){
                cost += e.getCost();
            }
            //System.out.println(p + " " + paths.get(p) + " " + cost);
            lowCost.put(p, cost);
        }
        Map<Integer, Double> sorted = sortByValues(lowCost);
        int num = 0;
        if (numPaths == -1) {
            for (Integer s : sorted.keySet()) {
                wtPaths.put(s, paths.get(s));
            }
        } else {
            for (Integer s : sorted.keySet()) {
                if (num++ == numPaths) {
                    break;
                }
                wtPaths.put(s, paths.get(s));
            }
        }
        System.out.println("------------------");
        for(Integer s:wtPaths.keySet()){
            System.out.println(s + " " + wtPaths.get(s));
        }
        
        return wtPaths;
    }
    
    public void showGO(List<String> go, OntologyUtils utils) throws IOException{
        for(String s:go){
            OntologyUtils.Term term = utils.getGOHierarchy().get(s);
            System.out.println(term.getId() + ": " + term.getName());
        }
    }
    
    public List<String> getGOfromPath(Map<String, Node> network, Set<Edge> path){
        Set<String> go = new LinkedHashSet<String>();
        for(Edge e:path){
            Node from = network.get(e.getFrom().getName());
            Node to = network.get(e.getTo().getName());
            System.out.println(to.getSymbol()+ " " + to.getCurrentGOs());
            //go.addAll(from.getGeneOntology());
            go.addAll(from.getCurrentGOs());
        }
        return new ArrayList<String>(go);
    }
    public List<String> getGOfromPaths(Map<String, Node> network, Map<Integer, List<Edge>> paths){
        Set<String> go = new LinkedHashSet<String>();
        for(Integer i:paths.keySet()){
            System.out.println(i + " " + paths.get(i));
            for(Edge e:paths.get(i)){
                Node from = network.get(e.getFrom().getName());
                go.addAll(from.getGeneOntology());
            }
        }
        return new ArrayList<String>(go);
    }
    
    public Map<Integer, List<Edge>> mergePaths(Map<Integer, List<Edge>> topSubnet1, 
            Map<Integer, List<Edge>> topSubnet2){
        
        Map<Integer, List<Edge>> combinedPaths = new LinkedHashMap<Integer, List<Edge>>();
        int n = 0;
        for(Integer i:topSubnet1.keySet()){
            for(Integer j:topSubnet2.keySet()){
                List<Edge> top1 = new ArrayList<Edge>(topSubnet1.get(i));
                List<Edge> top2 = reversePath(topSubnet2.get(j));
                top1.addAll(top2);
                combinedPaths.put(n, top1);
                n++;
            }
        }
        System.out.println("----------combined paths-----------");
        for(Integer k:combinedPaths.keySet()){
            double cost = 0.0;
            for(Edge e:combinedPaths.get(k)){
                cost += e.getCost();
            }
            System.out.println(k + ": " + combinedPaths.get(k) + " " + cost);
        }
        //System.out.println("combined paths: " + combinedPaths);
        
        return combinedPaths;
    }
    
    public Map<String, Node> buildSubnetFromPaths2(Collection<List<Edge>> paths){
        Map<String, Node> subnet = new LinkedHashMap<String, Node>();
        for(List<Edge> path:paths){
            //List<Edge> path = new ArrayList<Edge>(paths.get(k));
            for(int i = 0; i < path.size(); i++){
                Edge auxE = path.get(i);
                //if(auxE.getFrom().getName().equals("s")) continue;
                //if(auxE.getTo().getName().equals("t")) continue;
                
                Node from = new Node(auxE.getFrom().getName());
                from.setSymbol(auxE.getFrom().getSymbol());
                from.setCurrentGOs(auxE.getFrom().getCurrentGOs());
                from.setGeneOntology(auxE.getFrom().getGeneOntology());
                Node to = new Node(auxE.getTo().getName());
                to.setSymbol(auxE.getTo().getSymbol());
                to.setCurrentGOs(auxE.getTo().getCurrentGOs());
                to.setGeneOntology(auxE.getTo().getGeneOntology());

                if(!hasNode(subnet, from)) addNode(subnet, from);
                if(!hasNode(subnet, to)) addNode(subnet, to);
                
                Edge e = new Edge(from, to);
                e.setCapacity(auxE.getCapacity());
                e.setCost(auxE.getCost());
                e.setFlow(auxE.getFlow());
                
                if(!subnet.get(from.getName()).getEdges().contains(e)){
                    subnet.get(from.getName()).addEdge(e);
                }
                if(!subnet.get(to.getName()).getEdges().contains(e)){
                    subnet.get(to.getName()).addEdge(e);
                }
            }
        }
        System.out.println("------------subnet from paths----------");
        System.out.println(subnet.size());
        //printNetwork(subnet);
        return subnet;
    }
    
    public Map<String, Node> buildSubnetFromPaths(Map<Integer, List<Edge>> paths){
        Map<String, Node> subnet = new LinkedHashMap<String, Node>();
        for(Integer k:paths.keySet()){
            List<Edge> path = new ArrayList<Edge>(paths.get(k));
            for(int i = 0; i < path.size(); i++){
                Edge auxE = path.get(i);
                //if(auxE.getFrom().getName().equals("s")) continue;
                //if(auxE.getTo().getName().equals("t")) continue;
                
                Node from = new Node(auxE.getFrom().getName());
                from.setSymbol(auxE.getFrom().getSymbol());
                from.setCurrentGOs(auxE.getFrom().getCurrentGOs());
                from.setGeneOntology(auxE.getFrom().getGeneOntology());
                Node to = new Node(auxE.getTo().getName());
                to.setSymbol(auxE.getTo().getSymbol());
                to.setCurrentGOs(auxE.getTo().getCurrentGOs());
                to.setGeneOntology(auxE.getTo().getGeneOntology());

                if(!hasNode(subnet, from)) addNode(subnet, from);
                if(!hasNode(subnet, to)) addNode(subnet, to);
                
                Edge e = new Edge(from, to);
                e.setCapacity(auxE.getCapacity());
                e.setCost(auxE.getCost());
                e.setFlow(auxE.getFlow());
                
                if(!subnet.get(from.getName()).getEdges().contains(e)){
                    subnet.get(from.getName()).addEdge(e);
                }
                if(!subnet.get(to.getName()).getEdges().contains(e)){
                    subnet.get(to.getName()).addEdge(e);
                }
            }
        }
        System.out.println("------------subnet from paths----------");
        System.out.println(subnet.size());
        //printNetwork(subnet);
        return subnet;
    }
    
    public List<Edge> reversePath(List<Edge> path){
        Stack<Edge> reverseAux = new Stack<Edge>();
        List<Edge> reversePath = new ArrayList<Edge>();
        for(Edge e:path){
            if(e.getFrom().getName().equals("s")) continue;
            reverseAux.push(e);
        }
        while(!reverseAux.isEmpty()){
            reversePath.add(reverseAux.pop());
        }
        return reversePath;
    }
    
    public Map<Integer, Map<String, Edge>> combineSubnets(Map<Integer, Map<String, Edge>> pathsSub1,
            Map<Integer, Map<String, Edge>> pathsSub2){
        Map<Integer, Map<String, Edge>> newMap = new LinkedHashMap<Integer, Map<String, Edge>>();
        int k = 0;
        for(int i=0,j = 0; i < pathsSub1.size() && j < pathsSub2.size(); i++, j++){
            //Map<String, Edge> path = pathsSub1.get(i);
            newMap.put(k++, pathsSub1.get(i));
            newMap.put(k++, pathsSub2.get(j));
        }
        return newMap;
    }
    
    
    public Map<Integer, List<Edge>> lowCostPaths(Map<Integer, Map<String, Edge>> paths, 
            Map<String, Node> network, int numPaths){
        
        Map<Integer, Double> lowCost = new LinkedHashMap<Integer, Double>();
        Map<Integer, List<Edge>> top3Paths = new LinkedHashMap<Integer, List<Edge>>();
        
        for(int i = 0; i < paths.size(); i++){
            double cost = 0.0;
            Map<String, Edge> path = paths.get(i);
            for (String v = target.getName(); !v.equals(source.getName()); 
                    v = path.get(v).getOther(network.get(v)).getName()) {

                cost += path.get(v).getCost();
            }
            lowCost.put(i, cost);
        }
        Map<Integer, Double> sorted = sortByValues(lowCost);
        int numTopPaths = 0;
        for(Integer i:sorted.keySet()){
            List<Edge> pathEdges = new ArrayList<Edge>();
            //System.out.println("------------------------------");
            Map<String, Edge> path = paths.get(i);
            for (String v = target.getName(); !v.equals(source.getName()); 
                    v = path.get(v).getOther(network.get(v)).getName()) {
                pathEdges.add(path.get(v));
                //System.out.println(path.get(v));
            }
            //System.out.println("------------------------------");
            if(numPaths == -1){
                top3Paths.put(i, pathEdges);
            }else if(numTopPaths < numPaths){
                top3Paths.put(i, pathEdges);
                numTopPaths++;
            }
        }
        //System.out.println(lowCost);
        //System.out.println(sorted);
        return top3Paths;
    }
    /*
     * Java method to sort Map in Java by value e.g. HashMap or Hashtable
     * throw NullPointerException if Map contains null values
     * It also sort values even if they are duplicates
     */
    public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
        List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
     
        Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
     
        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
     
        for(Map.Entry<K,V> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
     
        return sortedMap;
    }

    
    public void show(Map<Integer, Map<String, Edge>> auxPath, Map<String, Node> network) {
        for (int i = 0; i < auxPath.size(); i++) {
            Map<String, Edge> path = auxPath.get(i);
            System.out.println("--------------------------------------");
            for (String v = target.getName(); !v.equals(source.getName()); v = path.get(v).getOther(network.get(v)).getName()) {
                System.out.println(path.get(v));
            }
            System.out.println("----------------------------------------");
        }
    }
    public Map<Integer, Map<String, Edge>> combinePaths(Map<Integer, Map<String, Edge>> pathSources, 
            Map<Integer, Map<String, Edge>> pathSinks){
        
        Map<Integer, Map<String, Edge>> combinedPaths = new LinkedHashMap<Integer, Map<String, Edge>>();
        Integer i = 0;
        for(int j = 0; j < pathSources.size(); j++){
            combinedPaths.put(i++, pathSources.get(j));
        }
        for(int j = 0; j < pathSinks.size(); j++){
            combinedPaths.put(i++, pathSinks.get(j));
        }
        return combinedPaths;
    }
    
    
    
    public void combineSubnets(//Map<String,Node> network,
            Map<String, Node> subnetCombined, 
            Map<String,Node> subnetSources,
            Map<String,Node> subnetSinks,
            String deletedGene){
        
        List<Edge> sourceEdges = subnetSources.get("t").getEdges();
        List<Edge> sinkEdges = subnetSinks.get("t").getEdges();
        List<Node> sourceProteins = new ArrayList<Node>();
        List<Node> sinkProteins = new ArrayList<Node>();
        for(Edge e:sourceEdges){
            Node from = new Node(e.getFrom().getName());
            from.setSymbol(e.getFrom().getSymbol());
            if(!sinkProteins.contains(from)){
                sourceProteins.add(from);
            }
        }
        for(Edge e:sinkEdges){
            Node from = new Node(e.getFrom().getName());
            from.setSymbol(e.getFrom().getSymbol());
            if(!sourceProteins.contains(from)){
                sinkProteins.add(from);
            }
        }
        Node s = new Node("s");
        Node t = new Node("t");
        s.setSymbol("s");
        t.setSymbol("t");
        this.addSourceAndTarget(subnetCombined, s, t, sourceProteins, sinkProteins);
    }
    
    public List<String> getGeneList(String file) throws IOException{
        List<String> geneList = new ArrayList<String>();
        BufferedReader in = null;
        int geneColumn = 1;
        try{
            in  = new BufferedReader(new FileReader(file));
            in.readLine();
            String line;
            while((line = in.readLine()) != null){
                String[] columns = line.split("\\,");
                String name = columns[geneColumn];
                String[] gene = name.split(" /// ");
                
                geneList.add(gene[0]);
                //numGenes++;
                //if(numGenes == 15) break;
                //Double fold = Double.valueOf(columns[fcColumn]);
                //if(fold >= 1.5){
                //geneList.add(name);
                //}
            }
            return geneList;
        }
        finally {
        	in.close(); 
        }

    }
    
    public static List<String> getSampleNames(String file) throws IOException{
        BufferedReader in = null;
        List<String> names = new ArrayList<String>();
        try{
            in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            String[] sampleNames = line.split("\t");
            for(String s:sampleNames){
                if(!s.equals("")){
                    names.add(s);
                }
            }
            return names;
        }
        finally {
        	in.close(); 
        }
    }
    
    public static List<String> getArrays(String file, 
            String state) throws IOException{
    
        BufferedReader in = null;
        //para cada experimento/replicata
        //First line contains gene and experiment names
        try{
            in = new BufferedReader(new FileReader(file));
            String line;
            line = in.readLine();
            List<String> firstLine = Arrays.asList(line.split("\t"));
            int index = firstLine.indexOf("description1");
            List<String> arrays = new ArrayList<String>();
            System.out.println(index + ": " + firstLine.get(index));
            while((line = in.readLine()) != null){
                String[] columns = line.split("\t");
                if(columns[index].equals(state)){
                    arrays.add(columns[0]);
                }
            }
            return arrays;
		} finally {
			in.close(); 

		}
    }
    
    public static List<String> getArrays(String file, 
            String state1, String state2) throws IOException{
    
        BufferedReader in = null;
        //para cada experimento/replicata
        //First line contains gene and experiment names
        try{
            in = new BufferedReader(new FileReader(file));
            String line;
            line = in.readLine();
            List<String> firstLine = Arrays.asList(line.split("\t"));
            int index = firstLine.indexOf("description1");
            List<String> arrays = new ArrayList<String>();
            System.out.println(index + ": " + firstLine.get(index));
            while((line = in.readLine()) != null){
                String[] columns = line.split("\t");
                if(columns[index].equals(state1) || columns[index].equals(state2)){
                    arrays.add(columns[0]);
                }
            }
            return arrays;
        }
        finally {
        	in.close(); 
        }
    }
    
    public void parseExpression(String file, 
            Map<String, Map<String, Double>> exp1,
            String[] exp1Rep) throws IOException{
    
        BufferedReader in = null;
        //para cada experimento/replicata
        //First line contains gene and experiment names
        try{
            in = new BufferedReader(new FileReader(file));
            int[] iExp1 = new int[exp1Rep.length];
            String line;
            line = in.readLine();
            List<String> firstLine = Arrays.asList(line.split("\t"));
            for (int i = 0; i < exp1Rep.length; i++) {
                int index = firstLine.indexOf(exp1Rep[i]);
                iExp1[i] = index;
                System.out.println(index + ": " + firstLine.get(iExp1[i]));
            }
            while((line = in.readLine()) != null){
                String[] columns = line.split("\t");
                Map<String, Double> map1 = new LinkedHashMap<String, Double>();
                for(int i = 0; i < iExp1.length; i++){
                    map1.put(exp1Rep[i], Double.valueOf(columns[iExp1[i]]));
                }
                exp1.put(columns[0], map1); //matrix de expressao celulas-tronco Warren coluna 0
            }
        }
        finally {
        	in.close();
        }
    }
    
    public Map<String, Double> calcMeanExpression(Map<String, Map<String, Double>> exp){
        Map<String, Double> geneMean = new LinkedHashMap<String, Double>();
        
        for(String s:exp.keySet()){
            List<Double> expression = new ArrayList<Double>(exp.get(s).values());
            //System.out.println(expression);
            double sum = 0.0;
            double mean = 0.0;
            for(Double d:expression){
                sum += d;
            }
            mean = sum / expression.size();
            geneMean.put(s, mean);
            //System.out.println(s + ": " + mean);
        }
        return geneMean;
    }

    public List<String> getSourcesSubnet(Map<String, Node> subnet){
        List<String> proteins = new ArrayList<String>();
        List<Edge> edges = subnet.get("s").getEdges();
        for(Edge e:edges){
            proteins.add(e.getTo().getName());
        }
        return proteins;
    }
    public ArrayList<String> getSinksSubnet(Map<String, Node> subnet){
        ArrayList<String> proteins = new ArrayList<String>();
        List<Edge> edges = subnet.get("t").getEdges();
        for(Edge e:edges){
            proteins.add(e.getFrom().getName());
        }
        return proteins;
    }
    
    public static Map<String, Integer> countGO(Map<String, Node> subnet){
        Map<String, Integer> counting = new LinkedHashMap<String, Integer>();
        for(String s:subnet.keySet()){
            Node n = subnet.get(s);
            if(s.equals("s")) continue;
            System.out.println("-----------" + n.getSymbol()+"----------");
            System.out.println("Total Flow: " + n.getTotalFlow());
            System.out.println("Out flow: " + n.getOutflow());
            //System.out.println("In flow: " + n.getInFlow());
            Map<String, Double> outflow = n.getTopOutFlow(); //get the ouflow genes from the gene s
            //System.out.println(n.getTopOutFlow().keySet());
            //Map<String, Integer> counting = new LinkedHashMap();
            for(String out:outflow.keySet()){
                //System.out.println(subnet.get(out).getSymbol());
                //System.out.println(subnet.get(out).getOutflowGOs());
                //System.out.println("---------");
                //System.out.println(subnetControl.get(out).getGeneOntology());
                //System.out.println("------------------------");
                //if(out.equals("s") || out.equals("t")) continue;
                if(out.equals("t")) continue;
                for(String g:subnet.get(out).getOutflowGOs()){
                    if(counting.containsKey(g)){
                        counting.put(g, counting.get(g) + 1);
                    }else{
                        counting.put(g, 1);
                    }
                }
            }
        }
        //System.out.println("counting: " + NetworkFlow.sortByValues(counting));
        return NetworkFlow.sortByValues(counting);
    }
    
    public static Map<String, Double> loadGOscores(String file) throws IOException{
        //String file="/home/edroaldo/Documents/Projects/NetDecoder_Method/Cancer/BreastCancer/GSE42568/sinks/TranscriptionFactors/ERpositive/GOscore_weighted_ERpositive_1.txt";
        Map<String, Double> randomGOscores = new LinkedHashMap<String, Double>();
        BufferedReader in = null;
        int goColumn = 1;
        int scoreColumn = 3;
        try{
            in  = new BufferedReader(new FileReader(file));
            in.readLine();
            //System.out.println(firstLine);
            String line;
            while((line = in.readLine()) != null){
                String[] columns = line.split("\t");
                String go = columns[goColumn];
                Double score = Double.valueOf(columns[scoreColumn]);
                randomGOscores.put(go, score);
            }
            return randomGOscores;
        }
        finally {
        	in.close(); 
        }

    }
    
    public static Map<String, Double> countGOWeighted(Map<String, Node> subnet){
        
        Map<String, Double> counting = new LinkedHashMap<String, Double>();
        for(String s:subnet.keySet()){
            Node n = subnet.get(s);
            if(s.equals("s")) continue;
            Map<String, Double> outflow = n.getTopOutFlow(); //get the ouflow genes from the gene s
            for(String out:outflow.keySet()){
                if(out.equals("t")) continue;
                for(String g:subnet.get(out).getOutflowGOs()){
                    if(counting.containsKey(g)){
                        Double score = (counting.get(g) + 1.0) * outflow.get(out);
                        counting.put(g, score);
                    }else{
                        counting.put(g, 1.0*outflow.get(out));
                    }
                }
            }
        }
        //System.out.println("counting: " + NetworkFlow.sortByValues(counting));
        return NetworkFlow.sortByValues(counting);
                //System.out.println("-----------" + n.getSymbol()+"----------");
            //System.out.println("Total Flow: " + n.getTotalFlow());
            //System.out.println("Out flow: " + n.getOutflow());
            //System.out.println("In flow: " + n.getInFlow());
    }
    
    public static List<String> getGenes(String file) throws IOException{
        List<String> geneList = new ArrayList<String>();
        BufferedReader in = null;
        int geneColumn = 0;
        try{
            in  = new BufferedReader(new FileReader(file));
            in.readLine();
            String line;
            while((line = in.readLine()) != null){
                String[] columns = line.split("\\,");
                String gene = columns[geneColumn];
                geneList.add(gene);
            }
            return geneList;
        }
        finally {
        	in.close();
        }

    }
    
    public Set<Integer> randomizeVector(List<String> proteins){
        Set<Integer> randomNumbers = new LinkedHashSet<Integer>();
        int size = proteins.size();
        int max = proteins.size();
        int min = 0;
        System.out.println("creating random numbers");
        while(randomNumbers.size() < size){
            Random rand = new Random();
            int randomNumber = rand.nextInt((max - min) + min);
            randomNumbers.add(randomNumber);
        }
        System.out.println("done");
        return randomNumbers;
    }
    
    public Set<String> getRandomProteins(List<String> proteins, int size){
        Set<String> randomProteins = new LinkedHashSet<String>();
        //int size = proteins.size();
        int max = proteins.size();
        int min = 0;
        System.out.println("obtaining " + size + " random proteins");
        while(randomProteins.size() < size){
            Random rand = new Random();
            int randomNumber = rand.nextInt((max - min) + min);
            randomProteins.add(proteins.get(randomNumber));
        }
        System.out.println("done");
        return randomProteins;
    }
    
    public List<List<Node>> selectSources(
            Set<String> sources,
            Set<String> sinks,
            List<String> proteinsA, 
            List<String> proteinsB){
    
        Map<String, Node> network = new LinkedHashMap();
        Set<Node> si = new LinkedHashSet();
        Set<Node> ti = new LinkedHashSet();
        for(int i = 0; i < proteinsA.size(); i++){
            Node proteinA = new Node(proteinsA.get(i));
            Node proteinB = new Node(proteinsB.get(i));
            proteinA.setSymbol(proteinA.getName());
            proteinB.setSymbol(proteinB.getName());
            
            Edge edge = new Edge(proteinA, proteinB);
            addEdge(network, edge);
        }
        for(String s:sources){
            Node so = network.get(s);
            if(so != null){
                si.add(so);
            }
        }
        
        for(String u:sinks){
            Node un = network.get(u);
            if(un != null){
                if(!ti.contains(un) && !si.contains(un)){
                    ti.add(un);
                }
            }
        }
        List<List<Node>> sourcesAndSinks = new ArrayList();
        sourcesAndSinks.add(new ArrayList(si));
        sourcesAndSinks.add(new ArrayList(ti));
        return sourcesAndSinks;
    }
    
    public List<List<Node>> selectSources(
            Set<String> sinks,
            List<String> proteinsA, 
            List<String> proteinsB){
    
        Map<String, Node> network = new LinkedHashMap();
        Set<Node> si = new LinkedHashSet();
        Set<Node> ti = new LinkedHashSet();
        System.out.println("selecting sources...");
        System.out.println(proteinsA.size());
        System.out.println(proteinsB.size());
        for(int i = 0; i < proteinsA.size(); i++){
            Node proteinA = new Node(proteinsA.get(i));
            Node proteinB = new Node(proteinsB.get(i));
            proteinA.setSymbol(proteinA.getName());
            proteinB.setSymbol(proteinB.getName());
            
            Edge edge = new Edge(proteinA, proteinB);
            addEdge(network, edge);
        }
        System.out.println("network size: " + network.size());
        Set<String> sinkNames = new LinkedHashSet();
        //System.out.println(sinks);
        for(String u:sinks){
            Node un = network.get(u);
            if(un != null){
                if(!ti.contains(un)){
                    ti.add(un);
                }
                sinkNames.add(un.getName());
            }
        }
        Set<String> aux = new LinkedHashSet(network.keySet());
        aux.removeAll(sinkNames);
        for(String i:aux){
            Node so = network.get(i);
            si.add(so);
        }
        System.out.println("souces: " + si.size());
        System.out.println("sinks: " + ti.size());
        List<List<Node>> sourcesAndSinks = new ArrayList();
        sourcesAndSinks.add(new ArrayList(si));
        sourcesAndSinks.add(new ArrayList(ti));
        return sourcesAndSinks;
    }
    
    public void createNetwork(
            List<Node> sources,
            List<Node> sinks,
            Map<String, Node> network,
            List<String> proteinsA, 
            List<String> proteinsB,
            List<Double> scores,
            List<Double> signScores){
        
        Node s = new Node("s");
        Node t = new Node("t");
        s.setSymbol("s");
        t.setSymbol("t");
        
        for(int i = 0; i < proteinsA.size(); i++){
            Node proteinA = new Node(proteinsA.get(i));
            Node proteinB = new Node(proteinsB.get(i));
            proteinA.setSymbol(proteinA.getName());
            proteinB.setSymbol(proteinB.getName());
            
            Edge edge = new Edge(proteinA, proteinB);
            edge.setCapacity(scores.get(i));
            edge.setCost(-Math.log(scores.get(i)));
            edge.setMiscore(scores.get(i));
            edge.setSignScore(signScores.get(i));
            addEdge(network, edge);
            //System.out.println(edge.getMiscore() + "\t" + edge.getSignScore());
        }
        System.out.println("Number of sources: " + sources.size());
        System.out.println("Number of sinks: " + sinks.size());
        System.out.println("Network size before: " + network.size());
        this.addSourceAndTarget(network, s, t, sources, sinks);
    }
    
    //Define sinks as transcription-related genes
    public Set<String> setUniversalSinksHuman(OntologyUtils utils, 
                                        List<String> annotation,
                                        String file) throws IOException{
        
        Set<String> universalSinks = new LinkedHashSet<String>();
        InputOutput io = new InputOutput();
        Map<String, List<String>> map = io.mapGOtxt(file);
        //System.out.println(map);
        
        for (String m : map.keySet()) {
            List<String> go = map.get(m);
            for (String a : annotation) {
                if (go.contains(a)) {
                    //if (hierarchy.get(a).namespace.equals("biological_process")) {
                        universalSinks.add(m);
                    //}
                }
            }
        }
        //20190617 cheng zhang: output the default sinks
        String fileout="./default_sinks_human.txt";
        File f = new File(fileout);
            if (!f.exists()) {
                f.createNewFile();
            }
        System.out.println("writing default sinks to file " + f.getAbsolutePath());
        FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        for(String m: universalSinks){
                bw.write(m + "\n");
            }
        bw.close();
        //end modification 20190617
        
        return universalSinks;
    }

    public void mappingNameToId(Map<String, Node> network){
        int i = 0;
        symbolTable = new LinkedHashMap<String, Integer>();
        for(String p:network.keySet()){
            symbolTable.put(p, i++);
        }
        keys = new String[symbolTable.size()];
        for(String s:symbolTable.keySet()){
            keys[symbolTable.get(s)] = s;
        }
    }
    
    
    public int E(){
        return E;
    }

    
    public void printNetwork(Map<String, Node> network){
        String s = "";
        for(String p:network.keySet()){
            //System.out.println("------------------: " + p);
            s += network.get(p).getSymbol() + ": ";
            for(Edge e:network.get(p).getEdges()){
                //s += e.getFrom().getName() + "-" +e.getTo().getName() + " ";
                s += e.getFrom().getSymbol() + "-" +e.getTo().getSymbol() + " ";// + e.getCost() + " ";
            }
            s += "\n"; 
        }
        System.out.println(s);
    }
    
    public void saveRandomNetworkInteractions(
            Map<String, List<String>> matrix,
            List<Double> miscores, String file) throws FileNotFoundException{
        
            PrintWriter pw = new PrintWriter(new FileOutputStream(file));
            int i = 0;
            for (String s : matrix.keySet()) {
                List<String> neighbors = matrix.get(s);
                for (String n : neighbors) {
                    pw.println(s + "\t" + n + "\t" + miscores.get(i++));
                }
            }
            pw.close();
    }

    public void saveSubNet(Map<String, Node> subnet, String file) {
        ArrayList<Edge> edges = new ArrayList<Edge>();
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(file));
            //pw.println("\nfrom\tto\tflow\tpath\ttype");
            pw.println("\nfrom\tto\tedge_flow\tcorrelation\tsign");
            for (String s : subnet.keySet()) {
                Node n = subnet.get(s);
                for (Edge e : n.getEdges()) {
                    //System.out.println("sign: " + e + "\t" +  e.getFlow() + "\t" + e.getSignScore() + "\t" + );
                    Integer sign = 0;
                    if(e.getSignScore() > 0){
                        sign = 1;
                    }else{
                        sign=-1;
                    }
                    if (!edges.contains(e)) {
                        pw.println(e.getFrom().getSymbol() + "\t"
                                + e.getTo().getSymbol() + "\t" + e.getFlow() + "\t" + 
                                e.getSignScore() + "\t" + sign);

                        edges.add(e);
                    }
                }
            }
            pw.close();
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        System.out.println("Subnet size: " + subnet.size());
    }
    
    public boolean hasNode(Map<String, Node> network, Node node){
        return network.containsKey(node.getName());
    }
    public boolean hasEdge(Map<String, Node> network, Edge edge){
        if(!hasNode(network, edge.getFrom())) return false;
        return network.get(edge.getFrom().getName()).getEdges().contains(edge);
    }
    
    public void addNode(Map<String, Node> network, Node node){
        if(!hasNode(network, node)){
            //network.put(node.getName(), new Node(node.getName()));
            network.put(node.getName(), node);
        }
    }
    
    public void addEdge(Map<String, Node> network, Edge edge){
        if(!hasEdge(network, edge)) E++;
        if(!hasNode(network, edge.getFrom())) addNode(network, edge.getFrom());
        if(!hasNode(network, edge.getTo())) addNode(network, edge.getTo());
        
        Edge edger = new Edge(edge.getTo(), edge.getFrom(), edge.getCapacity(), edge.getCost());
        edger.setMiscore(edge.getMiscore());
        edger.setSignScore(edge.getSignScore());
        
        network.get(edge.getFrom().getName()).addEdge(edge);
        network.get(edge.getFrom().getName()).addEdge(edger);
        network.get(edge.getTo().getName()).addEdge(edge);
        network.get(edge.getTo().getName()).addEdge(edger);
    }
    
    public Map<String, List<String>> filterGO(Map<String, OntologyUtils.Term> hierarchy, 
            Map<String, List<String>> map) throws IOException{
        
        
        Map<String, List<String>> newMap = new LinkedHashMap<String, List<String>>();
        for(String k:map.keySet()){
            ArrayList<String> newGO = new ArrayList<String>();
            for(String g:map.get(k)){
                
                OntologyUtils.Term term = hierarchy.get(g);
                if (term==null)//zc@20160821, add checking for null, i.e. GO in organism annotation does not exists in GO hierarchy.
                  {
                  System.out.println("null term for g - "+g+", skipped.");
                  continue;
                  }
                if(term.getNamespace().equals("biological_process")){ //term.getName()
                    newGO.add(term.getId());
                }
            }
            newMap.put(k, newGO);
        }
        return newMap;
    }
    
    public void setGOHuman(Map<String, Node> network, 
            OntologyUtils utils, String file) throws IOException {
        
        InputOutput io = new InputOutput();
        Map<String, List<String>> mapFull = io.mapGOtxt(file);
        //Map<String, List<String>> map = filterGO(utils.getGOHierarchy(), mapFull, fileIrrelevantGO);
        Map<String, OntologyUtils.Term> goh=utils.getGOHierarchy();
        Map<String, List<String>> map = filterGO(goh, mapFull);
        
        for(String n:network.keySet()){
            Node v = network.get(n);
            //String n = removePattern(u);
            //String n = u;
            if(n != null){
                if(map.containsKey(n)){
                    ArrayList<String> auxGO = new ArrayList<String>();
                    for(String s:map.get(n)){
                        auxGO.add(s);
                    }
                    v.setGeneOntology(auxGO);
                    v.setCurrentGOs(new ArrayList<String>(auxGO));
                }
            }
        }
    }
    
    public Map<Integer, List<Edge>> buildSubnet(Map<String, Node> network, 
            Map<String, Node> subnet,
            Map<Integer, Map<String, Edge>> allPaths){
        //Create a subnetwork connecting sources and targets through interactome edges
        //Aqui, network se refere a rede total, nao a subrede
        Map<Integer, Map<String, Edge>> auxPath = new LinkedHashMap<Integer, Map<String, Edge>>();
        int pathNum = 0;
        
        System.out.println("path size before: " + allPaths.size());
        for(int i = 0; i < allPaths.size(); i++){
            Map<String, Edge> path = allPaths.get(i);
            boolean flag = false;
            //System.out.println(i);
            for (String v = target.getName(); !v.equals(source.getName());
                    v = path.get(v).getOther(network.get(v)).getName()) {
                
                if(path.get(v).getFlow() == 0.0){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                auxPath.put(pathNum, path);
                pathNum++;
            }
        }
        System.out.println("path size after: " + auxPath.size());
        for (int i = 0; i < auxPath.size(); i++) {
            Map<String, Edge> path = auxPath.get(i);
            for (String v = target.getName(); !v.equals(source.getName());
                    v = path.get(v).getOther(network.get(v)).getName()) {
              
                Node from = new Node(path.get(v).getFrom().getName());
                Node to = new Node(v);
                from.setSymbol(network.get(from.getName()).getSymbol());
                from.setGeneOntology(network.get(from.getName()).getGeneOntology());
                from.setCurrentGOs(network.get(from.getName()).getCurrentGOs());
                from.setOutflowGOs(network.get(from.getName()).getOutflowGOs());
                from.setFunctionalCategory(network.get(from.getName()).getFunctionalCategory());
                
                to.setSymbol(network.get(to.getName()).getSymbol());
                to.setGeneOntology(network.get(to.getName()).getGeneOntology());
                to.setCurrentGOs(network.get(to.getName()).getCurrentGOs());
                to.setOutflowGOs(network.get(to.getName()).getOutflowGOs());
                to.setFunctionalCategory(network.get(to.getName()).getFunctionalCategory());
                
                if (!hasNode(subnet, from)) {
                    addNode(subnet, from);
                }
                if (!hasNode(subnet, to)) {
                    addNode(subnet, to);
                }

                Edge e = new Edge(from, to);
                e.setCapacity(path.get(v).getCapacity());
                e.setMiscore(path.get(v).getMiscore());
                e.setFlow(path.get(v).getFlow());
                e.setCost(path.get(v).getCost());
                e.setSignScore(path.get(v).getSignScore());
                
                if (!subnet.get(from.getName()).getEdges().contains(e)) {
                    subnet.get(from.getName()).addEdge(e);
                }
                if (!subnet.get(to.getName()).getEdges().contains(e)) {
                    subnet.get(to.getName()).addEdge(e);
                }
            }
        }
        Map<Integer, List<Edge>> pathsSubnet = getPaths(auxPath, subnet);
        return pathsSubnet;
        //return auxPath;
    }
    
    public Edge getEdge(Map<String, Node> network, Edge e){
        ArrayList<Edge> edges = network.get(e.getFrom().getName()).getEdges();
        System.out.println(edges);
         for(Edge es:network.get(e.getFrom().getName()).getEdges()){
            if(es.getFrom().getName().equals(e.getFrom().getName()) &&
                    es.getTo().getName().equals(e.getTo().getName())){
                return es;
            }
         }
        return null;
    }
    
    public void convergeGO2Sink(Map<String, Node> subnet, 
            Map<String, OntologyUtils.Term> hierarchy,
            Set<String> sinkGO){
        subnet.get("t").setCurrentGOs(new ArrayList<String>(sinkGO));
        System.out.println("----GOs propagated from source to sink---");
        for(String s:sinkGO){
            OntologyUtils.Term term = hierarchy.get(s);
            System.out.println(term.getId() + ": " + term.getName());
        }
    }
    
    public boolean compute(Map<String, Node> network, 
            Map<Integer, Map<String, Edge>> allPaths,
            OntologyUtils utils, Set<String> sinkGO) throws IOException{
        
        System.out.println("Computing flow");
        boolean augmented = false;
        if(!isFeasible(network, source, target)){
            System.out.println("Ferrou desde o inicio");
        }
        int i = 0;
        while(prim2(network, getNodesPath(), sinkGO)){
            processPath(network, getNodesPath(), i, allPaths);
            check(network, source, target);
            augmented = true;
            i++;
            if(allPaths.size() % 10 == 0){
                System.out.println("Number of paths: " + allPaths.size());
            }
            //if(allPaths.size() == 50) break;
        }   
     
        return augmented;
    }
    
    public void processPath(Map<String, Node> network, Map<String, NodeInfo> nodesPath, int path,
            Map<Integer, Map<String, Edge>> allPaths){
        double delta = Double.MAX_VALUE;

        for (String v = target.getName(); !v.equals(source.getName()); v = edgeTo.get(v).getOther(network.get(v)).getName()) {
            network.get(v).getOutflowGOs().addAll(network.get(v).getCurrentGOs()); //LINHA IMPORTANTE GO SCORE ORIGINAL
            delta = Math.min(delta, edgeTo.get(v).residualCapacityTo(network.get(v)));
        }
        //augment flow
        for (String v = target.getName(); !v.equals(source.getName()); v = edgeTo.get(v).getOther(network.get(v)).getName()) {
            edgeTo.get(v).addResidualFlowTo(network.get(v), delta);
        }
        //---Armazena todos os paths para depois contruir a subrede
        allPaths.put(path, new LinkedHashMap<String, Edge>(edgeTo));
        value += delta;
        //Codigo auxiliar para mostrar os caminhos escolhidos/encontrados.
        for (String v = target.getName(); !v.equals(source.getName()); v = edgeTo.get(v).getOther(network.get(v)).getName()) {
            if (edgeTo.get(v).getFlow() > 0) {
                if (!v.equals(source.getName()) || !v.equals(target.getName())) {
                    cost += edgeTo.get(v).getCost();
                }
            }
        }
        nodesPath.clear();
    }

    public boolean prim2(Map<String, Node> network, Map<String, NodeInfo> nodes, Set<String> sinkGO) {
        resetNodesVisited(network);
        ipq = new IndexMinPQ<Double>(network.size());
        Map<String, Boolean> inqueue = new LinkedHashMap<String, Boolean>();
        distTo = new LinkedHashMap<String, Double>();
        edgeTo = new LinkedHashMap<String, Edge>();
        
        for (String n : network.keySet()) {
            inqueue.put(n, false);
        }
        for (String n : network.keySet()) {
            if (n.equals(source.getName())) {
                distTo.put(source.getName(), 0.0);
                ipq.insert(symbolTable.get(source.getName()), distTo.get(source.getName()));
                inqueue.put(source.getName(), true);
            } else {
                distTo.put(n, Double.MAX_VALUE);

            }
        }
        while (!ipq.isEmpty()) {
            Node u = network.get(keys[ipq.delMin()]);
            inqueue.put(u.getName(), false);
            
            Iterator<Edge> it = u.edges();
            while (it.hasNext()) {
                Edge e = it.next();
                //Node v = e.getOther(u).getName()
                Node v = network.get(e.getOther(u).getName()); //obtem o Node que eh adjacente a u
                
                if (v.getName().equals(this.source.getName()) || v.getName().equals(u.getName())) continue;
                if (e.residualCapacityTo(v) > 0) {
                    //if (e.getFlow() < e.getCapacity()) {
                    double newDist = distTo.get(u.getName()) + Math.abs(e.getCost());
                    if (0 <= newDist && newDist < distTo.get(v.getName())) {
                        if (!checkGOFullContextDependent(network, u, v, sinkGO) ||
                                u.getName().equals("s") || v.getName().equals("t")) {
                            
                            nodes.put(v.getName(), new NodeInfo(u.getName(), true));
                            distTo.put(v.getName(), newDist);
                            edgeTo.put(v.getName(), e);
                            if (inqueue.get(v.getName())) {
                                ipq.decreaseKey(symbolTable.get(v.getName()), newDist);
                            } else {
                                ipq.insert(symbolTable.get(v.getName()), newDist);
                                inqueue.put(v.getName(), true);
                            }
                        }
                    }
                    if (v.getName().equals(target.getName())) {
                        return true;
                    }
                }
            }
        }

        return distTo.get(target.getName()) != Double.MAX_VALUE;
    }
    
    public Map<Integer, List<Edge>> getPaths(Map<Integer, Map<String, Edge>> paths, 
            Map<String, Node> network){
        
        Map<Integer, List<Edge>> allSinglePaths = new LinkedHashMap<Integer, List<Edge>>();
        for(int i = 0; i < paths.size(); i++){
            List<Edge> singlePath = new ArrayList<Edge>();
            Map<String, Edge> path = paths.get(i);
            for (String v = target.getName(); !v.equals(source.getName());
                    v = path.get(v).getOther(network.get(v)).getName()) {
                
                Edge e = path.get(v);
                singlePath.add(e);
                cost += e.getCost();
            }
            allSinglePaths.put(i, singlePath);
        }
        return allSinglePaths;
    }
    
    public Map<Integer, List<String>> showPaths(Map<Integer, Map<String, Edge>> paths, 
            Map<String, Node> network){
        
        Map<Integer, List<String>> allSinglePaths = new LinkedHashMap<Integer, List<String>>();
        for(int i = 0; i < paths.size(); i++){
            List<String> singlePath = new ArrayList<String>();
            Map<String, Edge> path = paths.get(i);
            for (String v = target.getName(); !v.equals(source.getName());
                    v = path.get(v).getOther(network.get(v)).getName()) {
                
                Edge e = path.get(v);
                if(e.getTo().getName().equals("t")){
                    singlePath.add(e.getTo().getName());
                }else if(e.getFrom().getName().equals("s")){
                    //System.out.print(e.getFrom().getSymbol());
                }
                //System.out.print(e.getFrom().getSymbol() + "->");
                
                singlePath.add(e.getFrom().getSymbol());
                cost += e.getCost();
            }
            allSinglePaths.put(i, singlePath);
            //sumCost += cost;
            //System.out.println("\nCost: " + cost);
        }
        //double meanCost = sumCost / paths.size();
        //System.out.println("Mean cost: " + meanCost);
        //System.out.println("Maxflow: " + value);
        
        return allSinglePaths;
    }
    
    
    public boolean shortestPaths(Map<String, Node> network, 
            Map<String, NodeInfo> nodes,
            Set<String> sinkGO){
        
        resetNodesVisited(network);
        //subEdgeTo = new LinkedHashMap<String, Edge>();
        edgeTo = new LinkedHashMap<String, Edge>();
        Queue<Node> queue = new Queue<Node>();
        source.setVisited(true);
        queue.enqueue(source);
        
        while(!queue.isEmpty()){
            Node u = network.get(queue.dequeue().getName());
            Iterator<Edge> it = u.edges();
            while(it.hasNext()){
                Edge e = it.next();
                Node v = network.get(e.getOther(u).getName());
                if (v.getName().equals(source.getName()) || v.getName().equals(u.getName())) continue;
                if(e.residualCapacityTo(v) > 0){
                    if (!v.isVisited()) {
                        if (!checkGOFullContextDependent(network, u, v, sinkGO) ||
                                u.getName().equals("s") || v.getName().equals("t")) {
                        
                            edgeTo.put(v.getName(), e);
                            v.setVisited(true);
                            queue.enqueue(v);
                        }
                    }
                    if (v.getName().equals(target.getName())) {
                        return true;
                    }
                }
            }
        }
        
        return target.isVisited();
        
        //return false;
    }
    
    public boolean checkGOHalfContextDependent(Map<String, Node> network, Node u, Node v, Set<String> sinkGO){
        ArrayList<String> vContext = new ArrayList<String>(v.getGOContext(network, 0.8));
        ArrayList<String> ont = new ArrayList<String>(union(vContext, v.getGeneOntology()));
        ArrayList<String> intersect = intersect(u.getCurrentGOs(), ont);

        if (u.getName().equals("s")) {
            v.setCurrentGOs(new ArrayList<String>(v.getGeneOntology()));
        } else if (v.getName().equals("t")) {
            //System.out.println("-------" + u.getSymbol() + "-------");
            v.setCurrentGOs(u.getCurrentGOs());
            for (String c : v.getCurrentGOs()) {
                sinkGO.add(c);
            }
        } else {
            v.setCurrentGOs(intersect);
        }
        return intersect.isEmpty();
    }
    /*Verifica se eu consigo fluir GO por este caminho*/
    //u eh a proteina anterior a v no caminho
    //Set<String> sinkGO = new LinkedHashSet<String>();
    public boolean checkGOFullContextDependent(Map<String, Node> network, Node u, Node v, Set<String> sinkGO){
        ArrayList<String> uContext = new ArrayList<String>(u.getGOContext(network, 0.95));
        ArrayList<String> vContext = new ArrayList<String>(v.getGOContext(network, 0.95));
        
        //ArrayList<String> uContext = new ArrayList<String>(u.getGOContext(network, 0.95));
        //ArrayList<String> vContext = new ArrayList<String>(v.getGOContext(network, 0.95));
        //ArrayList<String> uContext = new ArrayList<String>(u.getGOContext(network, 0.65));
        //ArrayList<String> vContext = new ArrayList<String>(v.getGOContext(network, 0.65));
        
        ArrayList<String> ont = new ArrayList<String>(union(vContext, v.getGeneOntology()));
        ArrayList<String> uvContext = intersect(uContext, vContext);
        for(String uv:uvContext){
            if(!network.get(u.getName()).getCurrentGOs().contains(uv)){
                network.get(u.getName()).getCurrentGOs().add(uv);
            }
        }
        ArrayList<String> intersect = intersect(u.getCurrentGOs(), ont);
        
        if(u.getName().equals("s")){
            network.get(v.getName()).setCurrentGOs(new ArrayList<String>(network.get(v.getName()).getGeneOntology()));
        }else if(v.getName().equals("t")){
            //System.out.println("-------" + u.getSymbol() + "-------");
            network.get(v.getName()).setCurrentGOs(network.get(u.getName()).getCurrentGOs());
            for(String c:v.getCurrentGOs()){
                sinkGO.add(c);
            }
        }else{
            if(!intersect.isEmpty()){
                //System.out.println(v.getSymbol() + " " + intersect);
                //network.get(v.getName()).getOutflowGOs().addAll(intersect);
                //System.out.println("outflowGO " + v.getSymbol() + ": " + network.get(v.getName()).getOutflowGOs());
                network.get(v.getName()).setCurrentGOs(intersect);
            }
        }
        //System.out.println("Intersection: " + intersect);
        return intersect.isEmpty();
    }
    
    public ArrayList<String> intersect(ArrayList<String> list1, ArrayList<String> list2){
        ArrayList<String> intersection = new ArrayList<String>();
        
        for(String s:list1){
            if(list2.contains(s)){
                intersection.add(s);
            }
        }
        
        return intersection;
    }
    public Set<String> union(ArrayList<String> list1, ArrayList<String> list2){
       Set<String> union = new LinkedHashSet<String>();
       union.addAll(list1);
       union.addAll(list2);
       return union;
    }
    public void resetNodesVisited(Map<String, Node> network){
        //Set<Node> nodes = getNodes();
        for(String n:network.keySet()){
            network.get(n).setVisited(false);
            ArrayList<String> go = new ArrayList<String>(network.get(n).getGeneOntology());
            network.get(n).setCurrentGOs(go);
        }
    }
    /*Proximo passo: extender para o um numero arbitrario de sources and sinks (targets)*/
    public void addSourceAndTarget(Map<String, Node> network, Node newSource, Node newTarget,List<Node> sources, List<Node> targets){
        //Basicamente, eu preciso atualizar a lista de adjancencia de cada Node in sources and targets
        //Verificar cuidadosamente como atribuir as capacidades entre (s0,si) e (ti,t0)
        //Node n1 = net.getNetwork().get("uniprotkb:P29366");
        List<Edge> edges = null;
        Node edgesSource = new Node(newSource.getName());
        Node edgesTarget = new Node(newTarget.getName());
        for(Node s:sources){
            edges = network.get(s.getName()).getEdges();
            double cap = 0.0;
            for (Edge e : edges) {
                cap += e.getCapacity();

            }
            Edge newEdge = new Edge(newSource, s, cap, 0);
            //System.out.println("Capacity Source; " + newEdge.getCapacity());
            edges.add(newEdge);
            edgesSource.addEdge(newEdge);    //source tem somente forward edges. (s0, si)
        }
        
        for(Node t:targets){
            //System.out.println("Target edges: ");
            if(network.get(t.getName()) == null){
                continue;
                
            }
            //System.out.println(network.get(t.getName()).getEdges());
            edges = network.get(t.getName()).getEdges();
            double cap = 0.0;
            for (Edge e : edges) {
                cap += e.getCapacity();
            }
            Edge newEdge = new Edge(t, newTarget, cap, 0);
            //System.out.println("Capacity Target; " + newEdge.getCapacity());
            edges.add(newEdge);
            edgesTarget.addEdge(newEdge);   //(ti,t0)
        }
        //Adicionar novos source e target a rede
        add(network, 0, newSource.getName(), edgesSource);
        network.put(newTarget.getName(), edgesTarget);
        network.get(newSource.getName()).setSymbol("s");
        network.get(newTarget.getName()).setSymbol("t");
        System.out.println("New source " + newSource.getSymbol() + " added");
        System.out.println("New target " + newTarget.getSymbol() + " added");
        this.setSource(newSource);
        this.setTarget(newTarget);
        System.out.println("Network size: " + network.size());
    }
    
    public void add(Map<String, Node> map, int index, String key, Node value){
        int i = 0;
        List<Entry<String, Node>> rest = new ArrayList<>();
        for(Entry<String, Node> entry:map.entrySet()){
            if(i++ >= index){
                rest.add(entry);
            }
        }
        map.put(key, value);
        for(int j = 0; j < rest.size(); j++){
            Entry<String,Node> entry = rest.get(j);
            map.remove(entry.getKey());
            map.put(entry.getKey(), entry.getValue());
        }
    }
    
    private boolean excess(Node v){
        double excess = 0.0;
        for(Edge e:v.getEdges()){
            if(v.getName().equals(e.getFrom().getName())){
                excess -= e.getFlow();
            }else{
                excess += e.getFlow();
            }
        }
        return Math.abs(excess) < 1E-11;
    }
    
    private boolean isFeasible(Map<String, Node> network, Node s, Node t){
        double EPSILON = 1E-11;
        
        //Check if the flow on edge is nonnegative and not greater than capacity
        for(String v:network.keySet()){
            for(Edge e:network.get(v).getEdges()){
                //if(e.getFlow() < 0 || e.getFlow() > e.getCapacity()){
                if(e.getFlow() < -EPSILON || e.getFlow() > e.getCapacity() + EPSILON){
                    System.out.println(e);
                    System.out.println("Capacity constraints are not satisfied");
                    return false;
                }
            }
        }
        for(String v:network.keySet()){
            if(!v.equals(source.getName()) && !v.equals(target.getName()) && !excess(network.get(v))){
                System.out.println("Flow equilibrium condition is not satisfied");
                return false;
            }
        }
        return true;
    }
    
    public boolean inCut(Node v){
        return v.isVisited();
    }
    
    public boolean check(Map<String, Node> network, Node s, Node t){
        if(!isFeasible(network, s, t)){
            System.out.println("Flow is not feasible");
            return false;
        }
        return true;
    }

    /**
     * @return the source
     */
    public Node getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(Node source) {
        this.source = source;
    }

    /**
     * @return the target
     */
    public Node getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(Node target) {
        this.target = target;
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * @return the nodesPath
     */
    public Map<String, NodeInfo> getNodesPath() {
        return nodesPath;
    }

    /**
     * @param nodesPath the nodesPath to set
     */
    public void setNodesPath(Map<String, NodeInfo> nodesPath) {
        this.nodesPath = nodesPath;
    }
}