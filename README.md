# Bachlors-Practical-Course-Data-Engineering
# The Evolution of Knowledge Graphs: How much has DBpedia changed in 10 years?

# Introduction

The DBpedia Knowledge Graph is built with a complex pipeline performing automatic content extraction from Wikipedia, which is mapped to a predefined ontology, but this automatic extraction can sometimes fail.

As an example, given David Bowie’s occupations in his Wikipedia infobox:
 Occupation     Singer . Songwriter . Musician . Actor
We can see two different properties:

- dbp:occupation (and, in general, the dbp: prefix) corresponds to raw data extracted from the infobox without an explicit mapping to the ontology. In this case, it somehow missed “musician”, and the objects are string literals instead of IRIs pointing to the occupations in DBpedia (e.g http://dbpedia.org/resource/Singer-songwriter)
    
dbp:occupation              . (en)
                            . Singer-Songwriter (en)
                            . actor (en)
                            
- dbo:occupation (and, in general, the dbo: prefix) corresponds to a successful mapping of the same raw data to DBpedia’s ontology. These predicates are preferred as they follow the ontology and their quality is higher, but they may not appear if the mappings failed.
    
    In this case, the mapping system failed again and created an entity for its occupation
    
  dbo:occupation          . dbr:David_Bowie_PersonFunction_1    
   

As another example, https://dbpedia.org/page/Michael_Schumacher does not even have occupation predicates, and we instead have to rely on its class to see that he was a Formula 1 driver.

This is currently a big concern in research, as any publication that used an specific version of DBpedia for their work (e.g for retrieving information, performing QA…) may have reproducibility issues (or just not work at all) with other versions of the KG, as the mappings or the quality of the triples may have changed wildly. The quality of these mappings (and of the underlying ontology) is supposed to improve on every edition, but in reality newer editions may have new *bugs.*

In this project, we want to analyze the differences in the ontology and mappings between the 2015 and 2022 editions of DBpedia. For this, I propose two different approaches (Note: The ideas are very open, as are the statistical analyses, plots and other visualizations you may want to perform!):

## Entity-focused analysis

In this approach, we would like to analyze mapping changes and the appearance of new facts on individual entities:

- By focusing on entities of famous people that passed away before 2015, we can observe changes in the ontology and the quality of the mappings
- By focusing on entities of famous people that are still actively edited, we can observe the appearance of new properties being added to them

It is also possible to obtain a ranking of the most popular entities (e.g via adaptions of PageRank to RDF…)

## Class-based analysis

In this approach, we would like to perform an statistical analysis of all entities associated to a few select classes of DBpedia (<dbo:Athlete>, <dbo:Company>…). This could be guided by the initial findings from the previous approach. For this, we can:

- Visualize the predicate distribution of all entities of the same class
- Compare predicate distributions of entities of different but similar classes (<dbo:Artist> and <dbo:Writer>…)
- Analyze the proportions of incorrect mappings
    - For example: Given a predicate which should point to IRIs, such as <dbo:occupation>, how many of them incorrectly point to literals? (This can be automatized if the predicate is of class <owl:ObjectProperty>, indicating that it should always point to IRIs, or if it is of class <owl:DatatypeProperty>, indicating that it should always point to datatypes)
- Extra: We could also employ Social Network Analysis metrics (https://cambridge-intelligence.com/social-network-analysis/) to analyze relations (grouped, or on a per-predicate basis) between entities of a given group. Are there big changes between versions?

By performing both of these analysis on the 2015 and 2022 versions, we could pinpoint important differences between them

# Additional information

- Class hierarchy of DBpedia:
    
    The main types are Person, Organisation and Place. You can visualize the ontology (in text) here: https://mappings.dbpedia.org/server/ontology/classes/
    
    
- The datasets and means to access them will be provided by us, and can be analyzed on an everyday computer
- Works that have performed similar analyses before:
    - An Evolutionary Analysis of DBpedia Datasets (Ideas for metrics, they did a general and class-based overview of several DBpedia versions)
        
         https://link.springer.com/chapter/10.1007/978-3-030-02934-0_30
        
    - An Analysis of the Quality Issues of the Properties Available in the Spanish DBpedia (ideas for metrics)
        
        https://link.springer.com/chapter/10.1007/978-3-319-24598-0_18
        
    - Assessing the Completeness Evolution of DBpedia: A Case Study (Use of completeness metrics from data mining)
        
        [https://link.springer.com/book/10.10s07/978-3-319-70625-2](https://link.springer.com/book/10.1007/978-3-319-70625-2)
        
    - Predicting incorrect mappings: a data-driven approach applied to DBpedia (ML model to predict incorrect mappings, although quite complex)
        
        https://dl.acm.org/doi/10.1145/3167132.3167164
        

# **SPARQL**

- A quick guide (for Wikidata, also applicable to DBpedia)
    
     - https://www.wikidata.org/wiki/Wikidata:SPARQL_tutorial#SPARQL_basics
    
- Example in DBpedia: Get the counts of each predicate (i.e., in how many triples they are present in) for entities whose type is dbo:Athlete

```sql
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX dbo: <http://dbpedia.org/ontology/>

SELECT ?p (COUNT(?p) AS ?count)
WHERE {
    ?s ?p ?o.
    ?s rdf:type dbo:Athlete.
}
GROUP BY ?p
ORDER BY DESC(?count)
```

# **Datasets**

- DBpedia’s SPARQL endpoint
    - https://dbpedia.org/sparql
    - Points to the live (2024) version
    - It is a public endpoint: Only use it for small queries, running long queries there is considered a bad practice and may hit timeouts
- 2015 and 2022 HDT files
    - 2015: https://tumde-my.sharepoint.com/:u:/g/personal/samuel_garcia_tum_de/ETbGaYC2dM9LsEpY3lFFvXcBT4lFuqq9EQx8d9QoKLvTbQ?e=rPQnyJ
    - 2022: [dbpedia_2022.zip](https://tumde-my.sharepoint.com/:u:/g/personal/samuel_garcia_tum_de/EQmzz2ySINVAg6Sk0P-UXzoBnQqRlQa57cOs_UohnRj2-A?e=J4ngBg)
    - When loading them using the libraries below, always point to the .hdt files. The .index files should be present in the same folder and will be automatically loaded by them
    - When performing any analysis on individual entities, remember to crosscheck with the 2024 SPARQL endpoint
        - If there are extremely striking differences (e.g. a famous person’s entity mysteriously missing too many important, non-metadata relations), please let me know - this may mean that the dataset dump that was used for the HDT files may have been incomplete

# Tools

- HDT format
    - General info: https://www.rdfhdt.org/what-is-hdt/
    - How HDT works (useful if you are going to iterate over the raw collection of triples)
        - https://www.rdfhdt.org/hdt-internals/
- Libraries
    - Python
        - https://github.com/RDFLib/rdflib-hdt
            - HDT extension for rdflib, a widely used library for working with RDF graphs
    - Java
        - https://github.com/rdfhdt/hdt-java
            - HDT extension for Jena, the equivalent of rdflib in Java
    - There is also a C++ implementation of HDT, but it **does not allow running SPARQL queries**, while the two above do
