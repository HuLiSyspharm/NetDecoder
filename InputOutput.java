/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netdecoder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author edroaldo
 */
public class InputOutput {
    private List<String> proteinsA;
    private List<String> proteinsB;
    private List<String> miscores;
    private List<Double> correlations;
    private List<Double> signCors;
    private String file;
    private final int PROTEINA = 0;
    private final int PROTEINB = 1;
    //private final int MISCORE = 14;
    private final int COR = 2;
    private final int SIGNCOR = 3;

    public InputOutput(){
        proteinsA = new ArrayList<String>();
        proteinsB = new ArrayList<String>();
        miscores = new ArrayList<String>();
        correlations = new ArrayList();
        signCors = new ArrayList();
    }
    public InputOutput(String file){
        super();
        this.file = file;
    }

    public void getColumnFromMITABFaster(String file) throws IOException{
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(file));
            String line;
            while((line = in.readLine()) != null){
                String columns[] = line.split("\t");
                //System.out.println(columns[0] + " " + columns[1] + " " + columns[2]);
                proteinsA.add(columns[PROTEINA]);
                proteinsB.add(columns[PROTEINB]);
                correlations.add(Double.valueOf(columns[COR]));
                signCors.add(Double.valueOf(columns[SIGNCOR]));
                //miscores.add(columns[COR]);
                //miscores.add(columns[MISCORE]);
            }
        }
        finally {
            in.close();
        }
    }
    public void getProteinsMITABFaster(String file) throws IOException{
        BufferedReader in = null;
        proteinsA.clear();
        proteinsB.clear();
        miscores.clear();
        try{
            in = new BufferedReader(new FileReader(file));
            String line;
            while((line = in.readLine()) != null){
                String columns[] = line.split("\t");
                proteinsA.add(columns[PROTEINA]);
                proteinsB.add(columns[PROTEINB]);
                miscores.add(columns[2]);
            }
        }
        finally {
            in.close();
        }
    }

    public Map<String, String> mapUniprot2txt(String file) throws IOException{
        Map<String, String> map = new LinkedHashMap<String, String>();
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(file));
            String line;
            while((line = in.readLine()) != null){
                String columns[] = line.split("\t");
                String uniprot = columns[1];
                String id = columns[2];
                map.put(uniprot, id);
            }
            return map;
        }
        finally {
            in.close();
        }
    }

    public Map<String, List<String>> mapGOtxt(String file) throws IOException{
        Map<String, List<String>> go = new LinkedHashMap<String, List<String>>();
        BufferedReader in = null;
        try{
            in = new BufferedReader(new FileReader(file));
            String line;
            while((line = in.readLine()) != null){
                String columns[] = line.split("\t");
                //String uniprot = columns[1];
                String uniprot = columns[2];
                String goID = columns[4];
                if(!go.containsKey(uniprot)){
                    List<String> gos = new ArrayList<String>();
                    gos.add(goID);
                    go.put(uniprot, gos);
                }else{
                    go.get(uniprot).add(goID);
                }
            }
            return go;
        }
        finally {
            in.close();
        }
    }

    public Map<String, Map<String, List<String>>> combineIdGO2(Map<String, List<String>> sgd, Map<String, List<String>> go){
        Map<String, Map<String, List<String>>> map = new LinkedHashMap<String, Map<String, List<String>>>();
        for (String s : sgd.keySet()) {
            Map<String, List<String>> n = new LinkedHashMap<String, List<String>>();
            String idSGD = sgd.get(s).get(0);
            if (go.containsKey(idSGD)) {
                List<String> l = go.get(idSGD);
                n.put(idSGD, l);
                map.put(s, n);
            }
        }
        return map;
    }

    public Map<String, List<String>> combineIdGO(Map<String, List<String>> sgd, Map<String, List<String>> go){
        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        for (String s : sgd.keySet()) {
            String idSGD = sgd.get(s).get(0);
            if (go.containsKey(idSGD)) {
                List<String> l = go.get(idSGD);
                map.put(s, l);
            }
        }
        return map;

    }

    /**
     * @return the proteinsA
     */
    public List<String> getProteinsA() {
        return proteinsA;
    }

    /**
     * @param proteinsA the proteinsA to set
     */
    public void setProteinsA(List<String> proteinsA) {
        this.proteinsA = proteinsA;
    }

    /**
     * @return the proteinsB
     */
    public List<String> getProteinsB() {
        return proteinsB;
    }

    /**
     * @param proteinsB the proteinsB to set
     */
    public void setProteinsB(List<String> proteinsB) {
        this.proteinsB = proteinsB;
    }

    /**
     * @return the miscores
     */
    public List<String> getMiscores() {
        return miscores;
    }

    /**
     * @param miscores the miscores to set
     */
    public void setMiscores(List<String> miscores) {
        this.miscores = miscores;
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the correlations
     */
    public List<Double> getCorrelations() {
        return correlations;
    }

    /**
     * @param correlations the correlations to set
     */
    public void setCorrelations(List<Double> correlations) {
        this.correlations = correlations;
    }

    /**
     * @return the signCors
     */
    public List<Double> getSignCors() {
        return signCors;
    }

    /**
     * @param signCors the signCors to set
     */
    public void setSignCors(List<Double> signCors) {
        this.signCors = signCors;
    }

}
