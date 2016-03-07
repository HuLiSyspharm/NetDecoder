/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netdecoder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author edroaldo
 */
public class NetDecoderUtils {
    
    public static void combineFiles(List<String> files, String combinedFile) {

        FileWriter fstream = null;
        BufferedWriter out = null;
        try {
            File mergedFile = new File(combinedFile);
            fstream = new FileWriter(mergedFile, true);
            out = new BufferedWriter(fstream);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        for (String file : files) {
            File f = new File(file);
            System.out.println("merging: " + f.getName());
            FileInputStream fis;
            try {
                fis = new FileInputStream(f);
                BufferedReader in = new BufferedReader(new InputStreamReader(fis));

                String aLine;
                while ((aLine = in.readLine()) != null) {
                    out.write(aLine);
                    out.newLine();
                }

                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    
    public static void getNumEdges(Map<String, Node> network){
        int numEdges = 0;
        for(String n:network.keySet()){
            Node p = network.get(n);
            numEdges += p.getEdges().size();
        }
        System.out.println("NUMBER OF EDGES" + numEdges);
    }
    
    public static void savePaths(List<Path> paths,
            String file) throws IOException {
        try {
            File f = new File(file);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("path\tpath_cost\tpath_flow");
            bw.newLine();
            for (Path p : paths) {
                Path rPath = p.reversePath();
                //bw.write(p.toString() + "\t" + p.cost + "\t" + p.flow);
                bw.write(rPath.toString() + "\t" + p.cost + "\t" + p.flow);
                bw.newLine();
            }
            bw.close();
        } catch (IOException io) {
        	throw new IOException("Trouble creating or writing to " + file);
        }
    }
    
    public static void saveTotalFlow(Map<String, Node> network, 
                                String head,
                                String file) throws IOException{
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
    
    public static void saveTotalFlowByCategory(Map<String, Node> network,
                                        String comparison,
                                        List<Node> proteins,
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
            for (Node n : proteins) {
                String aux = n.getSymbol().split("\\/")[0];
                //bw.write(comparison + "\t" + n.getSymbol() + "\t" + n.getTotalFlow());
                bw.write(comparison + "\t" + aux + "\t" + n.getTotalFlow());
                bw.newLine();
            }
            bw.close();
    
    }
    public static List<Node> getTopSources(Map<String, Node> subnet, int top){
        List<Node> proteins = new ArrayList();
        List<Edge> edges = subnet.get("s").getEdges();
        for(Edge e:edges){
            proteins.add(subnet.get(e.getTo().getName()));
        }
        Collections.sort(proteins, (Node n1, Node n2) -> Double.compare(n1.getTotalFlow(), n2.getTotalFlow()));
        List<Node> topProteins = proteins.subList(proteins.size() - top, proteins.size());
        return topProteins;
    }
    
    public static Map<String, Double> getSources(Map<String, Node> subnet, int top){
        List<Node> proteins = new ArrayList();
        List<Edge> edges = subnet.get("s").getEdges();
        for(Edge e:edges){
            proteins.add(subnet.get(e.getTo().getName()));
        }
        Collections.sort(proteins, (Node n1, Node n2) -> Double.compare(n1.getTotalFlow(), n2.getTotalFlow()));
        List<Node> auxProteins = null;
        if(proteins.size() < top + 1){
            auxProteins = proteins;
        }else{
            auxProteins = proteins.subList(proteins.size() - top, proteins.size());
        }
        Map<String, Double> topProteins = new LinkedHashMap();
        for(Node n:auxProteins){
            topProteins.put(n.getSymbol(), n.getTotalFlow());
        }
        return topProteins;
    }
    
    public static Map<String, Double> getTotalFlow(Map<String, Node> subnet){
        Map<String, Double> totalFlow = new LinkedHashMap();
        for(String n:subnet.keySet()){
            Node node = subnet.get(n);
            totalFlow.put(node.getSymbol(), node.getTotalFlow());
        }
        return totalFlow;
    }
    
    public static Map<String, Double> getSinks(Map<String, Node> subnet){
        List<Node> proteins = new ArrayList();
        List<Edge> edges = subnet.get("t").getEdges();
        for(Edge e:edges){
            proteins.add(subnet.get(e.getFrom().getName()));
        }
        Map<String, Double> totalFlow = new LinkedHashMap();
        for(Node n:proteins){
            totalFlow.put(n.getSymbol(), n.getTotalFlow());
        }
        return totalFlow;
    }

    public static Map<String, Double> getSources(Map<String, Node> subnet){
        List<Node> proteins = new ArrayList();
        List<Edge> edges = subnet.get("s").getEdges();
        for(Edge e:edges){
            proteins.add(subnet.get(e.getTo().getName()));
        }
        Map<String, Double> totalFlow = new LinkedHashMap();
        for(Node n:proteins){
            totalFlow.put(n.getSymbol(), n.getTotalFlow());
        }
        return totalFlow;
    }
    
    public static Map<String, Double> getHiddenProteins(Map<String, Node> subnet){
        List<String> proteins = new ArrayList(subnet.keySet());
        Set<String> sources = getSources(subnet).keySet();
        Set<String> sinks = getSinks(subnet).keySet();
        proteins.removeAll(sources);
        proteins.removeAll(sinks);
        proteins.remove("t");
        proteins.remove("s");
        Map<String, Double> totalFlow = new LinkedHashMap();
        for(String n:proteins){
            totalFlow.put(n, subnet.get(n).getTotalFlow());
        }
        return totalFlow;
    }
    
    public static Map<String, Double> getSinks(Map<String, Node> subnet, int top){
        List<Node> proteins = new ArrayList();
        List<Edge> edges = subnet.get("t").getEdges();
        for(Edge e:edges){
            proteins.add(subnet.get(e.getFrom().getName()));
        }
        Collections.sort(proteins, (Node n1, Node n2) -> Double.compare(n1.getTotalFlow(), n2.getTotalFlow()));
        List<Node> auxProteins = null;
        if(proteins.size() < top + 1){
            auxProteins = proteins;
        }else{
            auxProteins = proteins.subList(proteins.size() - top, proteins.size());
        }
        Map<String, Double> topProteins = new LinkedHashMap();
        for(Node n:auxProteins){
            topProteins.put(n.getSymbol(), n.getTotalFlow());
        }
        return topProteins;
    }
    
    public static List<Node> getTopSinks(Map<String, Node> subnet, int top){
        List<Node> proteins = new ArrayList();
        List<Edge> edges = subnet.get("t").getEdges();
        for(Edge e:edges){
            proteins.add(subnet.get(e.getFrom().getName()));
        }
        Collections.sort(proteins, (Node n1, Node n2) -> Double.compare(n1.getTotalFlow(), n2.getTotalFlow()));
        List<Node> topProteins = proteins.subList(proteins.size() - top, proteins.size());
        return topProteins;
    }
    
    public static List<String> transformPaths2String(List<Path> paths){
        List<String> auxPaths = new ArrayList();
        for(Path p:paths){
            auxPaths.add(convertPath2String(p.getPath(), true));
        }
        return auxPaths;
    }
    
    public static List<Path> transform2Paths(List<List<Edge>> paths){
        List<Path> newPaths = new ArrayList();
        for(List<Edge> path:paths){
            Path p = new Path(path);
            newPaths.add(p);
        }
        return newPaths;
    }
    
    public static int intersection(List<String> list1, List<String> list2){
        List<String> intersection = new ArrayList<String>();
        
        for(String s:list1){
            if(list2.contains(s)){
                intersection.add(s);
            }
        }
        return intersection.size();
    }
    
    public static Map<String, List<String>> mapGO2Genes(String file) throws IOException{
        Map<String, List<String>> go2genes = new LinkedHashMap<String, List<String>>();
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(file));
            String line;
            while((line = in.readLine()) != null){
                //System.out.println(line);
                String columns[] = line.split("\t");
                String uniprot = columns[1];
                String goID = columns[4];
                if(!go2genes.containsKey(goID)){
                    List<String> genes = new ArrayList<String>();
                    genes.add(uniprot);
                    go2genes.put(goID, genes);
                }else{
                    go2genes.get(goID).add(uniprot);
                }
            }
            return go2genes;
        }
        finally {
        	in.close();
        }
    }
    
    public static Map<String, List<String>> mapGO2uniprot(String file) throws IOException{
        Map<String, List<String>> go2genes = new LinkedHashMap<String, List<String>>();
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(file));
            String line;
            while((line = in.readLine()) != null){
                //System.out.println(line);
                String columns[] = line.split("\t");
                String uniprot = "uniprotkb:"+columns[1];
                String goID = columns[4];
                if(!go2genes.containsKey(goID)){
                    List<String> genes = new ArrayList<String>();
                    genes.add(uniprot);
                    go2genes.put(goID, genes);
                }else{
                    go2genes.get(goID).add(uniprot);
                }
            }
            return go2genes;
        }
        finally {
        	in.close();
        }
    }
    
    public static Map<String, List<String>> mapGenes2GO(String file) throws IOException{
        Map<String, List<String>> genes2go = new LinkedHashMap();
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(file));
            String line;
            while((line = in.readLine()) != null){
                //System.out.println(line);
                String columns[] = line.split("\t");
                String uniprot = columns[1];
                String goID = columns[4];
                if(!genes2go.containsKey(uniprot)){
                    List<String> gos = new ArrayList<String>();
                    gos.add(goID);
                    genes2go.put(uniprot, gos);
                }else{
                    genes2go.get(uniprot).add(goID);
                }
            }
            return genes2go;
        }
        finally {
        	in.close();
        }
    }
    
    /*public static void contributionScore(
            List<Map<String, Double>> genomeWideGOScores,
            List<Map<String, Map<String, Double>>> geneGOscores,
            List<String> diseaseStates,
            String realRandom,
            String filename) throws IOException{
        
        Map<String, Map<String, Double>> genomeWideContext = new LinkedHashMap();
        for(int i = 0; i < genomeWideGOScores.size(); i++){
            genomeWideContext.put(diseaseStates.get(i), genomeWideGOScores.get(i));
        }
        List<String> status = diseaseStates.subList(1, diseaseStates.size());
        System.out.println(status);
        Map<String, Map<String, Double>> genomeContext = getContext(genomeWideContext);
        Map<String, Map<String, Double>> cancerContributionScore = new LinkedHashMap();
        for (String er : status) {
            Map<String, Double> contributionScore = new LinkedHashMap();
            Map<String, Map<String, String>> gwDirection = 
                    getFlowDirection(genomeContext, diseaseStates.get(0), er);
            
            for (String gene : geneGOscores.get(0).keySet()) {
                Map<String, Map<String, Double>> auxGWASContext = new LinkedHashMap();
                for (int i = 0; i < geneGOscores.size(); i++) {
                    System.out.println(diseaseStates.get(i));
                    auxGWASContext.put(diseaseStates.get(i), geneGOscores.get(i).get(gene));
                }
                Map<String, Map<String, Double>> gwasGeneContext = getContext(auxGWASContext);
                System.out.println("--------------------- " + gene + " --------------------------");
                Map<String, Map<String, String>> gwasDirection = 
                        getFlowDirection(gwasGeneContext, diseaseStates.get(0), er);
                if(!gwasDirection.isEmpty()){ //verifica se contribui com o score
                    contributionScore.put(gene, calcContributionScore(gwDirection, gwasDirection));
                }
            }
            String name = filename + er + ".txt";
            saveCCS(contributionScore, er, realRandom, name);
            cancerContributionScore.put(er, contributionScore);
        }
        System.out.println("------showing contributionscores--------");
        for(String er:cancerContributionScore.keySet()){
            for(String gene:cancerContributionScore.get(er).keySet()){
                System.out.println(er + "\t" + gene + "\t" + cancerContributionScore.get(er).get(gene));
            }
            System.out.println("----------------------------");
        }
    }*/
    
    public static void saveCCS(
            Map<String, Double> contributionScore,
            String diseaseState,
            String file) throws IOException {
        
            File f = new File(file);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("expression" + "\t" + diseaseState);
            bw.newLine();
            boolean column = false;
            for (String gene : contributionScore.keySet()) {
                bw.write(gene + "\t" + contributionScore.get(gene));
                bw.newLine();
            }

            bw.close();
    }
    
    public static List<Path> getPhenotypeSpecificPaths(
            List<String> pathsDisease,
            List<String> pathsControl) {
        
        List<Path> auxDisease = convertString2Paths(pathsDisease);
        List<Path> auxControl = convertString2Paths(pathsControl);
        auxDisease.removeAll(auxControl);

        return auxDisease;
    }
    
    public static List<Path> convertString2Paths(List<String> pathsDisease) {
        
        List<Path> auxPaths = new ArrayList();
        for (String path : pathsDisease) {
            auxPaths.add(convertString2Path(path));
        }

        return auxPaths;
    }
    
    public static Map<Node, Map<Node, Integer>> createAdjacencyMatrix(Map<String, Node> network){
        Map<Node, Map<Node, Integer>> adj = new LinkedHashMap();
        for(String p1:network.keySet()){
            Node protein = network.get(p1);
            List<Node> neigh = getNeighbors(protein);
            Map<Node, Integer> aux = new LinkedHashMap();
            for(String p2:network.keySet()){
                Node protein2 = network.get(p2);
                if(neigh.contains(protein2)){
                    aux.put(protein2, 1);
                }else{
                    aux.put(protein2, 0);
                }
            }
            adj.put(protein, aux);
        }
        /*for(Node n1:adj.keySet()){
            System.out.print("\t" + n1.getSymbol());
        }
        System.out.println("");
        for(Node n1:adj.keySet()){
            System.out.println(n1.getSymbol());
            for(Node n2:adj.keySet()){
                System.out.print("\t" + adj.get(n1).get(n2));
            }
            System.out.println();
        }*/
        return adj;
    }
    
    public static List<Node> getNeighbors(Node protein){
        List<Node> neigh = new ArrayList();
        for(Edge e:protein.getEdges()){
            neigh.add(e.getOther(protein));
        }
        return neigh;
    }
    
    public static List<Map<String, Node>> getPathsFromGenes2(
            Map<String, Node> network,
            List<String> networkPaths,
            List<String> genes){
        
        List<Map<String, Node>> subnets = new ArrayList();
        for (String d : genes) {
            List<String> stringPath = new ArrayList();
            if (network.containsKey(d)) {
                Set<Edge> edges = new LinkedHashSet(network.get(d).getEdges());
                Map<Edge, List<Path>> edge2path = getPathsFromEdges(networkPaths, edges);
                for (Edge e : edge2path.keySet()) {
                    for (Path p : edge2path.get(e)) {
                        stringPath.add(convertPath2String(p.getPath(), true));
                    }
                }
            }
            if(stringPath.isEmpty()) continue;
            Map<String, Node> subnet = createNetworkFromPaths(stringPath);
            subnets.add(subnet);
       }
        return subnets;
    }
    
    public static List<String> getPathsFromGenes(
            Map<String, Node> network,
            List<String> networkPaths,
            List<String> genes, 
            RJava rJava,
            String dir,
            String filename) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
        
        List<String> scripts = new ArrayList();
        for (String d : genes) {
            List<String> stringPath = new ArrayList();
            if (network.containsKey(d)) {
                Set<Edge> edges = new LinkedHashSet(network.get(d).getEdges());
                Map<Edge, List<Path>> edge2path = getPathsFromEdges(networkPaths, edges);
                for (Edge e : edge2path.keySet()) {
                    for (Path p : edge2path.get(e)) {
                        stringPath.add(convertPath2String(p.getPath(), true));
                    }
                }
            }
            if(stringPath.isEmpty()) continue;
            Map<String, Node> subnet = createNetworkFromPaths(stringPath);
            //(new NetworkFlow()).printNetwork(subnet);
            //System.out.println(filename + "_" + d + ".txt");
            saveAdjMatrix(createAdjacencyMatrix(subnet), filename + "_" + d + ".txt");
            
            //rJava.plotAdjMatrix("", filename + "_" + d, d);
            scripts.add(rJava.createAdjAdjMatrixScript("", filename + "_" + d, d));
            
       }
        return scripts;
    }
    
    public static void getJaccardIndex(
            Map<String, Node> network,
            List<Node> nodes,
            String filename){
        
        Set<String> proteins = new LinkedHashSet();
        for(Node n:nodes){
            proteins.add(n.getName());
        }
        System.out.println("proteins: " + proteins.size());
        proteins.retainAll(network.keySet());
        proteins.retainAll(network.keySet());
        System.out.println("proteins: " + proteins.size());
        
        NetworkFlow net = new NetworkFlow();
        Map<String, Map<String, Double>> similarityMatrix = new LinkedHashMap();
        
        for (String c : proteins) {
            Node diseaseProtein = network.get(c);
            ArrayList<String> neigh = new ArrayList(diseaseProtein.getNeighborsBasedOnFlow(0.0));
            
            for (String e : proteins) {
                Node controlProtein = network.get(e);
                //if(controlProtein == null) continue;
                ArrayList<String> neigh2 = new ArrayList(controlProtein.getNeighborsBasedOnFlow(0.0));
                ArrayList<String> inter = net.intersect(neigh, neigh2);
                Set<String> union = net.union(neigh, neigh2);
                //System.out.println("------------------------------------");
                //System.out.println(inter.size() + " / " + union.size());
                double jaccard = 0.0;
                if (inter.isEmpty() && union.isEmpty()) {
                    jaccard = 1.0;
                } else {
                    jaccard = Double.valueOf(inter.size()) / Double.valueOf(union.size());
                }
                System.out.println(diseaseProtein.getSymbol() + "\t" + controlProtein.getSymbol() + "\t" + jaccard);
                //System.out.println("-----------------------------------");
                
                if(!similarityMatrix.containsKey(diseaseProtein.getSymbol())){
                    Map<String, Double> auxSimilarityMatrix = new LinkedHashMap();
                    auxSimilarityMatrix.put(controlProtein.getSymbol(), jaccard);
                    similarityMatrix.put(diseaseProtein.getSymbol(), auxSimilarityMatrix);
                }else{
                    similarityMatrix.get(diseaseProtein.getSymbol()).put(controlProtein.getSymbol(), jaccard);
                }
            }
            //similarityMatrix.put(diseaseProtein.getSymbol(), auxSimilarityMatrix);
           
        }
        saveJaccard(similarityMatrix, filename);
    }
    
    public static void getJaccardIndex(
            Map<String, Node> diseaseNetwork,
            Map<String, Node> controlNetwork,
            Set<Edge> keyEdges,
            String filename){
        
        Set<String> proteins = new LinkedHashSet();
        for(Edge e:keyEdges){
            proteins.add(e.getFrom().getName());
            proteins.add(e.getTo().getName());
        }
        System.out.println("proteins: " + proteins.size());
        proteins.retainAll(diseaseNetwork.keySet());
        proteins.retainAll(controlNetwork.keySet());
        System.out.println("proteins: " + proteins.size());
        
        NetworkFlow net = new NetworkFlow();
        Map<String, Map<String, Double>> similarityMatrix = new LinkedHashMap();
        
        for (String c : proteins) {
            Node diseaseProtein = diseaseNetwork.get(c);
            ArrayList<String> neigh = new ArrayList(diseaseProtein.getNeighborsBasedOnFlow(0.0));
            
            for (String e : proteins) {
                Node controlProtein = controlNetwork.get(e);
                //if(controlProtein == null) continue;
                ArrayList<String> neigh2 = new ArrayList(controlProtein.getNeighborsBasedOnFlow(0.0));
                ArrayList<String> inter = net.intersect(neigh, neigh2);
                Set<String> union = net.union(neigh, neigh2);
                //System.out.println("------------------------------------");
                //System.out.println(inter.size() + " / " + union.size());
                double jaccard = 0.0;
                if (inter.isEmpty() && union.isEmpty()) {
                    jaccard = 1.0;
                } else {
                    jaccard = Double.valueOf(inter.size()) / Double.valueOf(union.size());
                }
                System.out.println(diseaseProtein.getSymbol() + "\t" + controlProtein.getSymbol() + "\t" + jaccard);
                //System.out.println("-----------------------------------");
                
                if(!similarityMatrix.containsKey(diseaseProtein.getSymbol())){
                    Map<String, Double> auxSimilarityMatrix = new LinkedHashMap();
                    auxSimilarityMatrix.put(controlProtein.getSymbol(), jaccard);
                    similarityMatrix.put(diseaseProtein.getSymbol(), auxSimilarityMatrix);
                }else{
                    similarityMatrix.get(diseaseProtein.getSymbol()).put(controlProtein.getSymbol(), jaccard);
                }
            }
            //similarityMatrix.put(diseaseProtein.getSymbol(), auxSimilarityMatrix);
           
        }
        saveJaccard(similarityMatrix, filename);
    }
    
    public static void getJaccardIndex(
            Map<String, Node> diseaseNetwork,
            Map<String, Node> controlNetwork,
            String filename){
    
        Set<String> proteins = new LinkedHashSet(diseaseNetwork.keySet());
        proteins.retainAll(controlNetwork.keySet());
        
        NetworkFlow net = new NetworkFlow();
        Map<String, Map<String, Double>> similarityMatrix = new LinkedHashMap();
        for (String c : proteins) {
            Node diseaseProtein = diseaseNetwork.get(c);
            ArrayList<String> neigh = new ArrayList(diseaseProtein.getNeighborsBasedOnFlow(0.0));
            Map<String, Double> auxSimilarityMatrix = new LinkedHashMap();
            for (String e : proteins) {
                Node controlProtein = controlNetwork.get(e);
                ArrayList<String> neigh2 = new ArrayList(controlProtein.getNeighborsBasedOnFlow(0.0));
                ArrayList<String> inter = net.intersect(neigh, neigh2);
                Set<String> union = net.union(neigh, neigh2);
                //System.out.println("------------------------------------");
                //System.out.println(inter.size() + " / " + union.size());
                double jaccard;
                if (inter.isEmpty() && union.isEmpty()) {
                    jaccard = 1.0;
                } else {
                    jaccard = inter.size() / union.size();
                }
                //if(jaccard > 0.1){
                //    System.out.println(diseaseProtein.getSymbol() + "\t" + controlProtein.getSymbol() + "\t" + jaccard);
                    //System.out.println("-----------------------------------");
                //}
                auxSimilarityMatrix.put(controlProtein.getSymbol(), jaccard);
            }
            similarityMatrix.put(diseaseProtein.getSymbol(), auxSimilarityMatrix);
        }
        saveJaccard(similarityMatrix, filename);
    }
    
    
    public static Path convertString2Path(String auxPath){
        String[] auxEdges = auxPath.split("\\,");
        List<Edge> edges = new ArrayList();
        for(int i = 0; i < auxEdges.length; i++){
            Node from = new Node(auxEdges[i].split("\\->")[0]);
            Node to = new Node(auxEdges[i].split("\\->")[1]);
            from.setSymbol(from.getName());
            to.setSymbol(to.getName());
            Double flow = Double.valueOf(auxEdges[i].split("\\->")[2]);
            Double signScore = Double.valueOf(auxEdges[i].split("\\->")[3]);
            Double cost = Double.valueOf(auxEdges[i].split("\\->")[4]);
            Edge e = new Edge(from, to);
            e.setFlow(flow);
            e.setSignScore(signScore);
            e.setCost(cost);
            //System.out.println(e.getFlow() + "\t" + e.getSignScore());
            edges.add(e);
        }
        //System.out.println(edges);
        return new Path(edges);
    }
    
    //Create subnets from the paths containing top flow auxEdges for each edge and also combining all paths.
    public static Map<Edge, List<Path>> getPathsFromEdges(
            List<String> networkPaths,
            Set<Edge> edges){
        
        NetworkFlow net = new NetworkFlow();
        //List<String> allPathsFromEdges = new ArrayList();
        Map<Edge, List<Path>> edge2paths = new LinkedHashMap();
        for (Edge e : edges) {
            List<Path> paths = new ArrayList(); //store paths for each edge
            List<String> auxPaths = new ArrayList();
            for (String s : networkPaths) {
                Path path = convertString2Path(s);
                //System.out.println(e + ": " + path.getPath());
                if(path.getPath().contains(e)){
                    auxPaths.add(s);
                    paths.add(path);
                    //allPathsFromEdges.add(s);
                }
            }
            edge2paths.put(e, paths);
        }
        //Map<String, Node> combinedSubnet = createNetworkFromPaths(uniprot2symbol, allPathsFromEdges);
        //net.printNetwork(combinedSubnet);
        return edge2paths;
    }
    
    public static Map<Node, Map<Node, Double>> createMatrix(Map<String, Node> network){
        Map<Node, Map<Node, Double>> adj = new LinkedHashMap();
        for(String p1 : network.keySet()) {
            Node protein1 = network.get(p1);
            Map<Node, Double> aux = new LinkedHashMap();
            List<Node> neigh = NetDecoderUtils.getNeighbors(protein1);
            
            for (String p2 : network.keySet()) {
                Node protein2 = network.get(p2);
                if (neigh.contains(protein2)) {
                    Edge e1 = new Edge(protein1, protein2);
                    Edge e2 = new Edge(protein2, protein1);
                    Edge rE1 = NetDecoderUtils.getEdge(network, e1);
                    Edge rE2 = NetDecoderUtils.getEdge(network, e2);
                    if (rE1 != null) {
                        aux.put(protein2, rE1.getFlow());
                    }else{
                        aux.put(protein2, rE2.getFlow());
                    }
                } else {
                    aux.put(protein2, 0.0);
                }
            }
            adj.put(protein1, aux);
        }   
        
        return adj;
    }
    
    public static void saveMatrix(
            Map<Node, Map<Node, Double>> flowMatrix,
            String file) throws IOException {

        File f = new File(file);
        if (!f.exists()) {
            f.createNewFile();
        }
        FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        int i = 0;
        for (Node gene : flowMatrix.keySet()) {
            Map<Node, Double> netflow = flowMatrix.get(gene);
            if (i == 0) {
                for (Node protein : netflow.keySet()) {
                    String auxSymbol = protein.getSymbol().split("\\/")[0];
                    String symbol = auxSymbol.replace("-", "_");
                    bw.write("\t" + symbol);
                    //bw.write(protein.getSymbol() + "\t");
                }
                bw.newLine();
                i = 1;
            }
            //bw.write(gene.getSymbol() + "\t");
            String auxSymbol = gene.getSymbol().split("\\/")[0];
            String symbol = auxSymbol.replace("-", "_");
            bw.write(symbol);
            for (Node protein : netflow.keySet()) {
                bw.write("\t" + netflow.get(protein));
            }
            bw.newLine();
        }
        bw.close();
    }
    
    public static void comparePaths(
            Map<Edge, List<Path>> diseaseEdges,
            Map<Edge, List<Path>> controlEdges,
            Map<String, Double> totalFlowDisease,
            Map<String, Double> totalFlowControl,
            Map<String, Map<String, Double>> flowDifference,
            RJava rJava,
            String condition,
            Set<String> genesPaths,
            String filename,
            String dir,
            List<String> Rscripts) throws IOException{
        
        Set<String> controlPaths = new LinkedHashSet();
        Set<String> diseasePaths = new LinkedHashSet();
        
        for (Edge e : diseaseEdges.keySet()) {
            List<Path> auxDisease = new ArrayList(diseaseEdges.get(e));
            List<Path> auxControl = new ArrayList(controlEdges.get(e));
            for(Path p1:auxDisease){
                diseasePaths.add(convertPath2String(p1.getPath(), true));
            }
            for(Path p1:auxControl){
                controlPaths.add(convertPath2String(p1.getPath(), true));
            }
        }
        //Differential functional network analysis
        List<Path> diseaseSpecPaths = 
                NetDecoderUtils.getPhenotypeSpecificPaths(new ArrayList(diseasePaths), new ArrayList(controlPaths));
        List<Path> controlSpecPaths = 
                NetDecoderUtils.getPhenotypeSpecificPaths(new ArrayList(controlPaths), new ArrayList(diseasePaths));
        
        //create control and disease DFNs. Generate all results at the same time...
        Map<String, Node> controlNetwork = createNetworkFromPaths(new ArrayList(controlPaths));
        Map<String, Node> diseaseNetwork = createNetworkFromPaths(new ArrayList(diseasePaths));
        Map<String, Double> differences_disease = getFlowDifference(flowDifference, diseaseNetwork.keySet());
        Map<String, Double> differences_control = getFlowDifference(flowDifference, controlNetwork.keySet());;
        Map<String, Double> total_disease = getTotalFlowInProteins(totalFlowDisease, diseaseNetwork.keySet());
        Map<String, Double> total_control = getTotalFlowInProteins(totalFlowControl, controlNetwork.keySet());
    
        /*Map<Node, Map<Node, Double>> mControl = createMatrix(controlNetwork);
        Map<Node, Map<Node, Double>> mDisease = createMatrix(diseaseNetwork);
        saveMatrix(mControl, filename + "_matrix_Control.txt");
        saveMatrix(mDisease, filename + "_matrix_Disease.txt");*/
        
        saveFlows(total_control, filename + "_EDGE_CENTERED_SUBNET_totalFlow_Control.txt");
        saveFlows(total_disease, filename + "_EDGE_CENTERED_SUBNET_totalFlow_Disease.txt");
        saveFlows(differences_control, filename + "_EDGE_CENTERED_SUBNET_flowDifference_Control.txt");
        saveFlows(differences_disease, filename + "_EDGE_CENTERED_SUBNET_flowDifference_Disease.txt");
        saveGeneList(controlNetwork, filename + "_EDGE_CENTERED_SUBNET_GENES_Control.txt");
        saveGeneList(diseaseNetwork, filename + "_EDGE_CENTERED_SUBNET_GENES_Disease.txt");
        (new NetworkFlow()).saveSubNet(controlNetwork, filename+"_EDGE_CENTERED_SUBNET_Control.txt");
        (new NetworkFlow()).saveSubNet(diseaseNetwork, filename+"_EDGE_CENTERED_SUBNET_Disease.txt");
        savePaths(convertString2Paths(new ArrayList(diseasePaths)), filename+"_EDGE_CENTERED_SUBNET_Disease_paths.txt");
        savePaths(convertString2Paths(new ArrayList(controlPaths)), filename+"_EDGE_CENTERED_SUBNET_Control_paths.txt");
        
        //DFN results
        Map<String, Node> controlDFN = createNetworkFromPaths(convertPaths2String(controlSpecPaths));
        Map<String, Node> diseaseDFN = createNetworkFromPaths(convertPaths2String(diseaseSpecPaths));
        Map<String, Double> controlDFNdiff = getFlowDifference(flowDifference, controlDFN.keySet());
        Map<String, Double> diseaseDFNdiff = getFlowDifference(flowDifference, diseaseDFN.keySet());
        Map<String, Double> controlDFNtotal = getTotalFlowInProteins(totalFlowControl, controlDFN.keySet());
        Map<String, Double> diseaseDFNtotal = getTotalFlowInProteins(totalFlowDisease, diseaseDFN.keySet());
        saveFlows(controlDFNtotal, filename + "_DFN_totalFlow_Control.txt");
        saveFlows(diseaseDFNtotal, filename + "_DFN_totalFlow_Disease.txt");
        saveFlows(controlDFNdiff, filename + "_DFN_flowDifference_Control.txt");
        saveFlows(diseaseDFNdiff, filename + "_DFN_flowDifference_Disease.txt");
        saveGeneList(controlDFN, filename + "_DFN_GENES_Control.txt");
        saveGeneList(diseaseDFN, filename + "_DFN_GENES_Disease.txt");
        (new NetworkFlow()).saveSubNet(controlDFN, filename+"_DFN_Control.txt");
        (new NetworkFlow()).saveSubNet(diseaseDFN, filename+"_DFN_Disease.txt");
        savePaths(controlSpecPaths, filename + "_DFN_Control_paths.txt");
        savePaths(diseaseSpecPaths, filename + "_DFN_Disease_paths.txt");

        //saving Venn diagrams
        /*rJava.plotVennGenesPaths(dir, filename + "_EDGE_CENTERED_SUBNET_GENES_Control",
                filename + "_EDGE_CENTERED_SUBNET_GENES_Disease", condition, "gene", "gene_overlap");
        rJava.plotVennGenesPaths(dir, filename+"_EDGE_CENTERED_SUBNET_Control_paths",
                filename+"_EDGE_CENTERED_SUBNET_Disease_paths", condition, "path", "path_overlap");
        
        rJava.plotVennEdges(dir, filename+"_EDGE_CENTERED_SUBNET_Control",
                filename+"_EDGE_CENTERED_SUBNET_Disease", condition, "edge_overlap");
        
        //saving Venn diagrams DFNs
        rJava.plotVennGenesPaths(dir, filename + "_DFN_GENES_Control",
                filename + "_DFN_GENES_Disease", condition, "gene", "DFN_gene_overlap");
        rJava.plotVennGenesPaths(dir, filename+"_DFN_Control_paths",
                filename+"_DFN_Disease_paths", condition, "path", "DFN_path_overlap");
        rJava.plotVennEdges(dir, filename+"_DFN_Control",
                filename+"_DFN_Disease", condition, "DFN_edge_overlap");
                */
        String venn1 = rJava.createVennGenesPathsScript(dir, filename + "_EDGE_CENTERED_SUBNET_GENES_Control",
                filename + "_EDGE_CENTERED_SUBNET_GENES_Disease", condition, "gene", "gene_overlap");
        String venn2 = rJava.createVennGenesPathsScript(dir, filename+"_EDGE_CENTERED_SUBNET_Control_paths",
                filename+"_EDGE_CENTERED_SUBNET_Disease_paths", condition, "path", "path_overlap");
        
        String venn3 = rJava.createVennEdgesScript(dir, filename+"_EDGE_CENTERED_SUBNET_Control",
                filename+"_EDGE_CENTERED_SUBNET_Disease", condition, "edge_overlap");
        
        //saving Venn diagrams DFNs
        String venn4 = rJava.createVennGenesPathsScript(dir, filename + "_DFN_GENES_Control",
                filename + "_DFN_GENES_Disease", condition, "gene", "DFN_gene_overlap");
        String venn5 = rJava.createVennGenesPathsScript(dir, filename+"_DFN_Control_paths",
                filename+"_DFN_Disease_paths", condition, "path", "DFN_path_overlap");
        String venn6 = rJava.createVennEdgesScript(dir, filename+"_DFN_Control",
                filename+"_DFN_Disease", condition, "DFN_edge_overlap");

        Rscripts.add(venn1);
        Rscripts.add(venn2);
        Rscripts.add(venn3);
        Rscripts.add(venn4);
        Rscripts.add(venn5);
        Rscripts.add(venn6);
        //exporting networks to .gml files
        //rJava.exportGML(filename+"_EDGE_CENTERED_SUBNET_Control", filename + "_EDGE_CENTERED_SUBNET_totalFlow_Control", filename + "_EDGE_CENTERED_SUBNET_flowDifference_Control");
        //rJava.exportGML(filename+"_EDGE_CENTERED_SUBNET_Disease", filename + "_EDGE_CENTERED_SUBNET_totalFlow_Disease", filename + "_EDGE_CENTERED_SUBNET_flowDifference_Disease");
        String gml1 = rJava.createScriptExportGML(filename+"_EDGE_CENTERED_SUBNET_Control", filename + "_EDGE_CENTERED_SUBNET_totalFlow_Control", filename + "_EDGE_CENTERED_SUBNET_flowDifference_Control");
        String gml2 = rJava.createScriptExportGML(filename+"_EDGE_CENTERED_SUBNET_Disease", filename + "_EDGE_CENTERED_SUBNET_totalFlow_Disease", filename + "_EDGE_CENTERED_SUBNET_flowDifference_Disease");
        
        //exporting DFNs
        //rJava.exportGML(filename+"_DFN_Control", filename + "_DFN_totalFlow_Control", filename + "_DFN_flowDifference_Control");
        //rJava.exportGML(filename+"_DFN_Disease", filename + "_DFN_totalFlow_Disease", filename + "_DFN_flowDifference_Disease");
        String gml3 = rJava.createScriptExportGML(filename+"_DFN_Control", filename + "_DFN_totalFlow_Control", filename + "_DFN_flowDifference_Control");
        String gml4 = rJava.createScriptExportGML(filename+"_DFN_Disease", filename + "_DFN_totalFlow_Disease", filename + "_DFN_flowDifference_Disease");
        Rscripts.add(gml1);
        Rscripts.add(gml2);
        Rscripts.add(gml3);
        Rscripts.add(gml4);
        
        if (genesPaths != null) {
            System.out.println("Saving paths associated to a given gene list...");
            try {
                List<String> tmpDisease = new ArrayList(convertPaths2String(diseaseSpecPaths));
                List<String> tmpControl = new ArrayList(convertPaths2String(controlSpecPaths));
                List<String> adjDisease = getPathsFromGenes(diseaseDFN, tmpDisease, new ArrayList(genesPaths), rJava,
                        dir, filename + "_Disease");
                List<String> adjControl = getPathsFromGenes(controlDFN, tmpControl, new ArrayList(genesPaths), rJava,
                        dir, filename + "_Control");
                Rscripts.addAll(adjDisease);
                Rscripts.addAll(adjControl);
            } catch (Exception io) {
                System.out.println(io.getMessage());
            }
        }
        NetDecoderUtils.getJaccardIndex(diseaseNetwork, controlNetwork, filename + "_EDGE_CENTERED_SUBNET_Jaccard_" + condition + ".txt");
        //rJava.plotJaccardMatrix(filename + "_EDGE_CENTERED_SUBNET_Jaccard_" + condition);
        String jaccard = rJava.createJaccardMatrixScript(filename + "_EDGE_CENTERED_SUBNET_Jaccard_" + condition);
        Rscripts.add(jaccard);
    }
    
    public static Map<String, Double> getTotalFlowInProteins(
            Map<String, Double> totalFlows,
            Set<String> genes){
        
        Map<String, Double> total = new LinkedHashMap();
        for(String gene:genes){
            total.put(gene, totalFlows.get(gene));
        }
        return total;
    }
    
    public static Map<String, Double> getFlowDifference(
            Map<String, Map<String, Double>> totalFlows,
            Set<String> genes){
        Map<String, Double> difference = new LinkedHashMap();
        for(String gene:genes){
            difference.put(gene, totalFlows.get(gene).get("difference"));
        }
        return difference;
    }
    
    public static List<String> convertPaths2String(List<Path> paths){
        List<String> newPaths = new ArrayList();
        for(Path p:paths){
            newPaths.add(convertPath2String(p.getPath(), true));
        }
        return newPaths;
    }
    
    public static double calcFlowFromPaths(List<List<Edge>> paths) {
        double flow = 0.0;
        for (List<Edge> p : paths) {
            flow += getFlowFromPath(p);
        }
        return flow;
    }
    public static double getFlowFromPath(List<Edge> path){
        double flow = 0.0;
        for (Edge e : path) {
            if (e.getFrom().getName().equals("s") || e.getTo().getName().equals("t")) continue;
            flow += e.getFlow();
        }
        return flow;
    }
    
    public static double getPathCost(List<Edge> path){
        double cost = 0.0;
        for (Edge e : path) {
            if (e.getFrom().getName().equals("s") || e.getTo().getName().equals("t")) continue;
            cost += e.getCost();
        }
        return cost;
    }

    public static double calculateDistance(
            double[] array1,
            double[] array2){
        double sum = 0.0;
        for(int i=0;i<array1.length;i++) {
           sum = sum + Math.pow((array1[i]-array2[i]),2.0);
        }
        return Math.sqrt(sum);
    }
    
    public static double calculateDistance(
            Map<String, Double> array1,
            Map<String, Double> array2,
            String[] arrays){
        
        double sum = 0.0;
        for(String array:arrays){
            sum = sum + Math.pow(array1.get(array) - array2.get(array), 2.0);
        }
        return Math.sqrt(sum);
    }
    
    public static double calculateDistance(
            double value1,
            double value2){
        
        return Math.sqrt(Math.pow(value1 - value2, 2.0));
    }

    public static Map<String, Double> averageFlow(
            Map<String, Map<String, Double>> edgesMatrix,
            Set<String> edges,
            String[] arrays){
        
        Map<String, Double> meanFlow = new LinkedHashMap();
        for(String edge:edges){
            double sum = 0.0;
            for(String array:arrays){
                sum += edgesMatrix.get(edge).get(array);
            }
            double mean = sum / arrays.length;
            meanFlow.put(edge, mean);
        }
        
        return meanFlow;
    }
    
    public static List<Double> calcZscore(
            Map<String, Double> edgeFlows){
        
        double mean = getMean(edgeFlows.values());
        double std = getStdDev(edgeFlows.values());
        List<Double> zscore = new ArrayList();
        for(String array:edgeFlows.keySet()){
            double tmp = (edgeFlows.get(array) - mean) / std;
            zscore.add(tmp);
        }
        return zscore;
    }
    
    public static Map<String,Double> calcZscoreEdge(
            Map<String, Double> edgeFlows){
        
        double mean = getMean(edgeFlows.values());
        double std = getStdDev(edgeFlows.values());
        Map<String, Double> zscore = new LinkedHashMap();
        for(String edge:edgeFlows.keySet()){
            double tmp = (edgeFlows.get(edge) - mean) / std;
            zscore.put(edge, tmp);
        }
        return zscore;
    }
    public static double calcMeanZscore(
            Map<String, Double> edgeFlows,
            String[] arrays){
        
        List<Double> zscore = calcZscore(edgeFlows);
        double meanZscore = getMean(zscore);
        return meanZscore;
    }
    
    public static double getMean(Collection<Double> data){
        double sum = 0.0;
        for(double d:data){
            sum += d;
        }
        return sum / data.size();
    }

    public static double getVariance(Collection<Double> data){
        double mean = getMean(data);
        double temp = 0;
        for(double a:data)
            temp += (mean-a)*(mean-a);
        return temp/data.size();
    }

    public static double getStdDev(Collection<Double> data){
        return Math.sqrt(getVariance(data));
    }

    public Map<String, Map<String, Double>> changeMapping(
            Map<String, Map<String, Double>> edgesMatrix){
        
        //invert the mappings from array->edge->flow to edge->array->flow
        Map<String, Map<String, Double>> auxEdgesMatrix = new LinkedHashMap();
        for (String array : edgesMatrix.keySet()) {
            for (String edge : edgesMatrix.get(array).keySet()) {
                if (!auxEdgesMatrix.containsKey(edge)) {
                    Map<String, Double> aux2 = new LinkedHashMap();
                    aux2.put(array, edgesMatrix.get(array).get(edge));
                    auxEdgesMatrix.put(edge, aux2);
                } else {
                    auxEdgesMatrix.get(edge).put(array, edgesMatrix.get(array).get(edge));
                }
            }
        }
        return auxEdgesMatrix;
    }
    
    public static void saveAllPaths(String[] arrays, 
            Map<String, Map<String, String>> uniprot2symbol,
            String dir){
    
        NetworkFlow net = new NetworkFlow();
        Map<Integer, List<Edge>> paths = null;
        for (int array = 0; array < arrays.length; array++) {
            List<String> pathsFlow = new ArrayList();   
            String experiment = arrays[array];
            String fileSubnetPaths = "subnetPaths_" + experiment + ".ser";
            paths = Serialization.deserialize(dir+fileSubnetPaths, Map.class);
            for (Integer path : paths.keySet()) {
                String pathFlowAsString = convertPath2String(paths.get(path), true);
                pathsFlow.add(pathFlowAsString);
            }
            Serialization.serialize(pathsFlow, dir+"subnetPaths_" + experiment + "_compact.ser");
        }
    }
    
    public static Map<String, Node> createMotifTargets(String gene, List<Edge> edges){
        Map<String, Node> subnet = new LinkedHashMap();
        NetworkFlow net = new NetworkFlow();

        for (Edge e : edges) {
            Node from = new Node(e.getFrom().getName());
            Node to = new Node(e.getTo().getName());
            if (from.getName().equals("s") || to.getName().equals("t")) {
                continue;
            }
            if (to.getName().equals(gene)) {
                from.setSymbol(from.getName());
                to.setSymbol(to.getName());

                Edge edge = new Edge(from, to);
                edge.setFlow(e.getFlow());
                edge.setSignScore(e.getSignScore());

                if (!net.hasNode(subnet, from)) {
                    net.addNode(subnet, from);
                }
                if (!net.hasNode(subnet, to)) {
                    net.addNode(subnet, to);
                }

                if (!subnet.get(from.getName()).getEdges().contains(e)) {
                    subnet.get(from.getName()).addEdge(e);
                }
                if (!subnet.get(to.getName()).getEdges().contains(e)) {
                    subnet.get(to.getName()).addEdge(e);
                }
            }
        }
        return subnet;
    }
    
    public static Map<String, Node> createMotifNetworkRouters(String gene, List<Edge> edges){
        Map<String, Node> subnet = new LinkedHashMap();
        NetworkFlow net = new NetworkFlow();

        for (Edge e : edges) {
            Node from = new Node(e.getFrom().getName());
            Node to = new Node(e.getTo().getName());
            if (from.getName().equals("s") || to.getName().equals("t")) {
                continue;
            }
            if (from.getName().equals(gene)) {
                from.setSymbol(from.getName());
                to.setSymbol(to.getName());

                Edge edge = new Edge(from, to);
                edge.setFlow(e.getFlow());
                edge.setSignScore(e.getSignScore());

                if (!net.hasNode(subnet, from)) {
                    net.addNode(subnet, from);
                }
                if (!net.hasNode(subnet, to)) {
                    net.addNode(subnet, to);
                }

                if (!subnet.get(from.getName()).getEdges().contains(e)) {
                    subnet.get(from.getName()).addEdge(e);
                }
                if (!subnet.get(to.getName()).getEdges().contains(e)) {
                    subnet.get(to.getName()).addEdge(e);
                }
            }
        }
        return subnet;
    }
    
    public static Map<String, Node> createMotifImpactScore(List<Edge> edges){
        Map<String, Node> subnet = new LinkedHashMap();
        NetworkFlow net = new NetworkFlow();
        
        for (Edge e : edges) {
            Node from = new Node(e.getFrom().getName());
            Node to = new Node(e.getTo().getName());
            if(from.getName().equals("s") || to.getName().equals("t")) continue;

            from.setSymbol(from.getName());
            to.setSymbol(to.getName());
            
            Edge edge = new Edge(from, to);
            edge.setFlow(e.getFlow());
            edge.setSignScore(e.getSignScore());
            
            if(!net.hasNode(subnet, from)) net.addNode(subnet, from);
            if(!net.hasNode(subnet, to)) net.addNode(subnet, to);
                
            if (!subnet.get(from.getName()).getEdges().contains(e)) {
                subnet.get(from.getName()).addEdge(e);
            }
            if (!subnet.get(to.getName()).getEdges().contains(e)) {
                subnet.get(to.getName()).addEdge(e);
            }
        }
        return subnet;
    }
    
    public static Map<String, Node> createNetworkFromPaths(List<String> paths){
        
        Map<String, Node> subnet = new LinkedHashMap();
        NetworkFlow net = new NetworkFlow();
        
        for(String auxPath:paths){
            Path path = convertString2Path(auxPath);
            for(int i = 0; i < path.getPath().size(); i++){
                Edge auxE = path.getPath().get(i);
                //if(auxE.getFrom().getName().equals("s")) continue;
                //if(auxE.getTo().getName().equals("t")) continue;
                Node from = new Node(auxE.getFrom().getName());
                from.setSymbol(auxE.getFrom().getSymbol());
                Node to = new Node(auxE.getTo().getName());
                to.setSymbol(auxE.getTo().getSymbol());
                
                if(!net.hasNode(subnet, from)) net.addNode(subnet, from);
                if(!net.hasNode(subnet, to)) net.addNode(subnet, to);
                
                Edge e = new Edge(from, to);
                e.setFlow(auxE.getFlow());
                e.setSignScore(auxE.getSignScore());
                
                if(!subnet.get(from.getName()).getEdges().contains(e)){
                    subnet.get(from.getName()).addEdge(e);
                }
                if(!subnet.get(to.getName()).getEdges().contains(e)){
                    subnet.get(to.getName()).addEdge(e);
                }
            }
        }
        //System.out.println("------------subnet from paths----------");
        //System.out.println(subnet.size());
        //printNetwork(subnet);
        return subnet;
    }
    
    public static Map<String, Node> createNetworkFromPaths2Plot(List<String> paths){
        
        Map<String, Node> subnet = new LinkedHashMap();
        NetworkFlow net = new NetworkFlow();
        
        for(String auxPath:paths){
            Path path = convertString2Path(auxPath);
            for(int i = path.getPath().size() - 1; i >= 0; i--){
                Edge auxE = path.getPath().get(i);
                if (i > 0) {
                    Edge nextEdge = path.getPath().get(i - 1);
                    if (!auxE.getTo().getName().equals(nextEdge.getFrom().getName())) {
                        Edge tmpEdge = new Edge(nextEdge.getTo(), nextEdge.getFrom());
                        tmpEdge.setFlow(nextEdge.getFlow());
                        tmpEdge.setSignScore(nextEdge.getSignScore());
                        path.getPath().set(i - 1, tmpEdge);
                    }
                }
                //if(auxE.getFrom().getName().equals("s")) continue;
                //if(auxE.getTo().getName().equals("t")) continue;
                Node from = new Node(auxE.getFrom().getName());
                from.setSymbol(auxE.getFrom().getSymbol());
                Node to = new Node(auxE.getTo().getName());
                to.setSymbol(auxE.getTo().getSymbol());
                
                if(!net.hasNode(subnet, from)) net.addNode(subnet, from);
                if(!net.hasNode(subnet, to)) net.addNode(subnet, to);
                
                Edge e = new Edge(from, to);
                e.setFlow(auxE.getFlow());
                e.setSignScore(auxE.getSignScore());
                
                if(!subnet.get(from.getName()).getEdges().contains(e)){
                    subnet.get(from.getName()).addEdge(e);
                }
                if(!subnet.get(to.getName()).getEdges().contains(e)){
                    subnet.get(to.getName()).addEdge(e);
                }
            }
        }
        //System.out.println("------------subnet from paths----------");
        //System.out.println(subnet.size());
        //printNetwork(subnet);
        return subnet;
    }

    public static List<Double> splitEdgesFlow(String path){
        String[] edges = path.split("\\,");
        List<Double> flows = new ArrayList();
        for(int i = 0; i < edges.length; i++){
            Double flow = Double.valueOf(edges[i].split("\\->")[2]);
            flows.add(flow);
        }
        return flows;
    }
    public static List<Double> splitEdgesCost(String path){
        String[] edges = path.split("\\,");
        List<Double> flows = new ArrayList();
        for(int i = 0; i < edges.length; i++){
            Double flow = Double.valueOf(edges[i].split("\\->")[3]);
            flows.add(flow);
        }
        return flows;
    }
    
    
    public static String convertPath2String(List<Edge> path, boolean flow){
        String s = "";
        for(int i = 0; i < path.size(); i++){
        //for(Edge e:path){
            Edge e = path.get(i);
            //String[] s1 =  e.getFrom().getName().split("\\/");
            //String[] s2 =  e.getTo().getName().split("\\/");
            String s1 =  e.getFrom().getName();
            String s2 =  e.getTo().getName();
            if(flow){
                if(i == path.size() - 1){
                    s += s1 + "->" + s2 + "->" + e.getFlow() + "->" + e.getSignScore() + "->" + e.getCost();
                }else{
                    s += s1 + "->" + s2 + "->" + e.getFlow() + "->" + e.getSignScore() + "->" + e.getCost() + ",";
                }
            }else{
                if(i == path.size() - 1){
                    s += s1 + "->" + s2;
                }else{
                    s += s1 + "->" + s2 + ",";
                }
            }
        }
        return s;
    }
    
    //--------------------------------------------------------------------------
    
    public static Edge getEdge(Map<String, Node> network, Edge e) {
        if (network.get(e.getFrom().getName()) == null) {
            return null;
        } else {
            for (Edge es : network.get(e.getFrom().getName()).getEdges()) {
                if (es.getFrom().getName().equals(e.getFrom().getName())
                        && es.getTo().getName().equals(e.getTo().getName())) {
                    return es;
                }
            }
        }
        return null;
    }
    
    public static String edge2String(Edge edge){
        String s1 = edge.getFrom().getSymbol();
        String s2 = edge.getTo().getSymbol();
        return s1 + "->" + s2;
    }
    public static String edge2String2(Edge edge){
        String s1 = edge.getFrom().getName();
        String s2 = edge.getTo().getName();
        return s1 + "->" + s2;
    }

    public static void saveAdjMatrix(
            Map<Node, Map<Node, Integer>> adjMatrix,
            String file) throws IOException{
        
            File f = new File(file);
            if(!f.exists()){
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            
            int i = 0;
            bw.write("gene");
            for (Node gene : adjMatrix.keySet()) {
                Map<Node, Integer> netflow = adjMatrix.get(gene);
                if(i == 0){
                    for(Node protein:netflow.keySet()){
                        String auxSymbol = protein.getSymbol().split("\\/")[0];
                        String symbol = auxSymbol.replace("-", "_");
                        bw.write("\t" + symbol);
                        //bw.write(protein.getSymbol() + "\t");
                    }
                    bw.newLine();
                    i = 1;
                }
                //bw.write(gene.getSymbol() + "\t");
                String auxSymbol = gene.getSymbol().split("\\/")[0];
                String symbol = auxSymbol.replace("-", "_");
                bw.write(symbol);
                for (Node protein : netflow.keySet()) {
                    bw.write("\t" + netflow.get(protein));
                }
                bw.newLine();
            }
            bw.close();
    }
    
    
    public static void saveFlows(
            Map<String, Double> differences,
            String file) throws IOException {
        
        try{
            File f = new File(file);
            if(!f.exists()){
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("gene\tflows");
            bw.newLine();
            for (String gene : differences.keySet()) {
                bw.write(gene + "\t" + differences.get(gene));
                bw.newLine();
            }
            bw.close();
        }catch(IOException e){
			throw new IOException("Error creating or writing " + file, e);
        }
    }
    
    public static void saveGeneList(
            Map<String, Node> network,
            String file) throws IOException {

        try {
			File f = new File(file);
			if (!f.exists()) {
			    f.createNewFile();
			}
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("gene");
			bw.newLine();
			for (String gene : network.keySet()) {
			    String symbols = network.get(gene).getSymbol();
			    String symbol = symbols.split("\\/")[0];
			    bw.write(symbol);
			    //bw.write(network.get(gene).getSymbol());
			    bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			throw new IOException("Error creating or writing " + file, e);
		}
    }

    public static void saveData(
            Map<String, Map<String, Double>> data,
            String file) {
        //Map<String, OntologyUtils.Term> utils = new OntologyUtils().getGOHierarchy();
        try {
            File f = new File(file);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            //for(String s:mapping.keySet()){
            //    bw.write("\t" + s);
            //}
            bw.newLine();
            boolean column = false;
            for (String c : data.keySet()) {
                if (!column) {
                    for (String g : data.get(c).keySet()) {
                        bw.write("\t" + g);
                    }
                    column = true;
                    bw.newLine();
                }

                bw.write(c);
                for (String g : data.get(c).keySet()) {
                    //System.out.println(c + "\t" + g + "\t" + mapping.get(c).get(g));
                    bw.write("\t" + data.get(c).get(g));
                }
                bw.newLine();

            }
            bw.close();
        } catch (IOException io) {
        }

    }


public static void saveJaccard(
            Map<String, Map<String, Double>> similarityMatrix,
            String file) {
        //Map<String, OntologyUtils.Term> utils = new OntologyUtils().getGOHierarchy();
        try {
            File f = new File(file);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            //for(String s:mapping.keySet()){
            //    bw.write("\t" + s);
            //}
            bw.newLine();
            boolean column = false;
            for (String c : similarityMatrix.keySet()) {
                if (!column) {
                    for (String g : similarityMatrix.get(c).keySet()) {
                        bw.write("\t" + g);
                    }
                    column = true;
                    bw.newLine();
                }

                bw.write(c);
                for (String g : similarityMatrix.get(c).keySet()) {
                    //System.out.println(c + "\t" + g + "\t" + mapping.get(c).get(g));
                    bw.write("\t" + similarityMatrix.get(c).get(g));
                }
                bw.newLine();

            }
            bw.close();
        } catch (IOException io) {
        }
    }
}