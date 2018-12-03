package pl.kaszaq.howfastyouaregoing.agile.jira;

import pl.kaszaq.howfastyouaregoing.agile.jira.examples.*;
import java.util.HashMap;
import objectexplorer.MemoryMeasurer;
import objectexplorer.ObjectGraphMeasurer;
import objectexplorer.ObjectGraphMeasurer.Footprint;
import pl.kaszaq.howfastyouaregoing.agile.AgileClient;
import pl.kaszaq.howfastyouaregoing.agile.AgileProject;

public class MemoryMeasure {

    /*
    @ rev 1584c146f026a993527fa3d3892507aab3b9ab8b
    615656
Footprint [objects=15813, references=37008, primitives={char=83553, int=9434, short=1644, long=822, boolean=822, float=273, byte=2037}]
    
    @After change to list instead of tree sets
    588392
Footprint [objects=14990, references=33620, primitives={float=273, short=1644, char=83553, boolean=272, long=822, int=9434, byte=2037}]
    */
    public static void main(String[] args) {
        
        AgileClient agileClient = AgileClientProvider.createClient();

        AgileProject project = agileClient.getAgileProject("AWW");

        System.out.println(MemoryMeasurer.measureBytes(project));
        System.out.println(ObjectGraphMeasurer.measure(project));

    }
}
