/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdecoder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author edroaldo
 */
public class OntologyUtils {

    private BufferedReader in;
    private String buffer;
    private Map<String, Term> id2term = new LinkedHashMap<String, Term>();
    
    public OntologyUtils(String file) throws IOException{
        parse(file);
    }
    
    public static void main(String args[]) throws IOException {
        String file = "/home/edroaldo/Documents/Projects/Yeast/gene_ontology_ext_FULL.obo";
        OntologyUtils utils = new OntologyUtils(file);
        //utils.parse(file);
        System.out.println(utils.id2term.size());
        Set<String> leafs = new LinkedHashSet<String>();
        for (String i : utils.id2term.keySet()) {
            Term term = utils.id2term.get(i);
            if (term.getNamespace().equals("biological_process")) { //cellular_component biological_process
                System.out.println(term.getId());
                //System.out.println(term.name);
                System.out.println("Parents: " + term.is_a);
                System.out.println("Children: " + term.children);
                utils.dfsLeafs(term.getId(), leafs);
            }
        }
        
        System.out.println("----------------Searching---------------");
        
        /*System.out.println("Parents: ");
        System.out.println(utils.getDepth("GO:0019236", 2, true));
        System.out.println("Children: ");
        System.out.println(utils.getDepth("GO:0019236", 6, false));
        System.out.println("----------------------");
        System.out.println(utils.getDepth("GO:0019236", 1, false));
        */
        System.out.println("Leafs");
        System.out.println(leafs);
        System.out.println(leafs.size());
        //Set<String> leafs = new LinkedHashSet<String>();
        //System.out.println(utils.dfsLeafs("GO:2001313", leafs));
    }
    
    public Set<String> getDepth(String s, int depth, boolean parents){
        Set<String> depthParents = new LinkedHashSet<String>();
        Set<String> depthChildren = new LinkedHashSet<String>();
        
        if(parents){
            dfsParents(s, depth, depthParents);
            return depthParents;
        }
        else{
            dfsChildren(s, depth, depthChildren);
            return depthChildren;
        }
    }

    public Set<String> getDepth(String s, int depth) {
        Set<String> depthGOs = new LinkedHashSet<String>();
        dfsParents(s, depth, depthGOs);
        dfsChildren(s, depth, depthGOs);

        return depthGOs;
    }
    
    public void dfsParents(String s, int depth, Set<String> depthParents) {
        if (depth > 0) {
            for (String w : getTerm(s, false).is_a) { //for each term parent in the GO hierarchy
                depthParents.add(w);
                //System.out.println(w);
                dfsParents(w, depth - 1, depthParents);
            }
        }
    }
    public void dfsChildren(String s, int depth, Set<String> depthChildren) {
        if (depth > 0) {
            for (String w : getTerm(s, false).children) { //for each term parent in the GO hierarchy
                depthChildren.add(w);
                //System.out.println(w);
                dfsParents(w, depth - 1, depthChildren);
            }
        }
    }
    
    public Set<String> dfsLeafs(String s, Set<String> leafs) {
        if (getTerm(s,false).getNamespace().equals("biological_process") &&
                getTerm(s, false).children.isEmpty()) {
            leafs.add(s);
        }else{
            for (String w : getTerm(s, false).children) { //for each term parent in the GO hierarchy
                dfsLeafs(w, leafs);
            }
        }
        return leafs;
    }

    public Map<String, Term> getGOHierarchy() throws IOException{
        return this.id2term;
    }

    public class Term {
        private String id;
        private String name;
        private String namespace;
        private String def;
        Set<String> children = new LinkedHashSet<String>();
        Set<String> is_a = new LinkedHashSet<String>();
        //Set<String> relationship = new LinkedHashSet<String>();

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the namespace
         */
        public String getNamespace() {
            return namespace;
        }

        /**
         * @param namespace the namespace to set
         */
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        /**
         * @return the def
         */
        public String getDef() {
            return def;
        }

        /**
         * @param def the def to set
         */
        public void setDef(String def) {
            this.def = def;
        }
    }

    private Term getTerm(String id, boolean create) {
        Term term = id2term.get(id);
        if (term == null && create) {
            term = new Term();
            term.setId(id);
            term.setName(id);
            term.setNamespace(id);
            term.setDef(id);
            id2term.put(id, term);
        }
        return term;
    }

    private String nocomment(String s) {
        int remove = s.indexOf("!");
        if (remove != -1) {
            s = s.substring(0, remove);
        }
        return s.trim();
    }

    private String next() throws IOException {
        if (buffer != null) {
            String s = buffer;
            buffer = null;
            return s;
        }
        return in.readLine();
    }

    private void parse(String file) throws IOException {
        //String file = "/home/edroaldo/Documents/Projects/Yeast/gene_ontology_ext_02May2014.obo";
        //String file = "/home/edroaldo/Documents/Projects/Yeast/gene_ontology_ext_FULL.obo";
        InputStream inStream = new FileInputStream(file);
        in = new BufferedReader((new InputStreamReader(inStream)));
        String line;
        while ((line = next()) != null) {
            if (line.equals("[Term]")) {
                parseTerm();
            }
        }
        in.close();
    }

    private void parseTerm() throws IOException {
        Term term = null;
        String line;
        while ((line = next()) != null) {
            if (line.startsWith("[")) {
                this.buffer = line;
                break;
            }
            int colon = line.indexOf(":");
            if (colon == -1) {
                continue;
            }
            if (line.startsWith("id:") && term == null) {
                String GOId = line.substring(colon + 1).trim();
                term = getTerm(GOId, true);
                continue;
            }
            if (term == null) {
                continue;
            }
            if (line.startsWith("name:")) {
                String name = nocomment(line.substring(colon + 1));
                term.setName(name);
            } else if (line.startsWith("namespace:")) {
                String namespace = nocomment(line.substring(colon + 1));
                term.setNamespace(namespace);
            } else if (line.startsWith("def:")) {
                String def = nocomment(line.substring(colon + 1));
                term.setDef(def);
            } else if (line.startsWith("is_a:")) {
                String parentGO = nocomment(line.substring(colon + 1));
                term.is_a.add(parentGO);
                Term parent = getTerm(parentGO, true);
                parent.children.add(term.getId());
            }
        }
    }
}
