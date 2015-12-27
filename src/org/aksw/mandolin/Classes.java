package org.aksw.mandolin;

import org.aksw.mandolin.NameMapper.Type;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Classes {
	
	private final static Cache size = new Cache();
	
	/**
	 * @param map 
	 * @param SRC_PATH
	 * @param TGT_PATH
	 */
	public static void build(final NameMapper map, final String BASE) {
		
		// reader implementation
		StreamRDF dataStream = new StreamRDF() {

			@Override
			public void base(String arg0) {
			}

			@Override
			public void finish() {
			}

			@Override
			public void prefix(String arg0, String arg1) {
			}

			@Override
			public void quad(Quad arg0) {
			}

			@Override
			public void start() {
			}

			@Override
			public void triple(Triple arg0) {
				String s = arg0.getSubject().getURI();
				String p = arg0.getPredicate().getURI();
				String o = arg0.getObject().toString();

				// if property is rdf:type...
				if(p.equals(RDF.type.getURI())) {
					// then object is always a class
					String className = map.add(o, Type.CLASS);
					// if object is :Class...
					if(o.equals(OWL.Class.getURI()) ||
							o.equals(RDFS.Class.getURI())) {
						// then also subject is a class
						map.add(s, Type.CLASS);
					} else {
						// else subject is an entity
						String entName = map.add(s, Type.ENTITY);
						map.addEntClass(entName, className);
					}
				}

				size.value++;
			}

		};

		RDFDataMgr.parse(dataStream, BASE + "/model-fwc.nt");
		
		map.setCollisionDelta(collisionDelta());
		
	}
	
	/**
	 * Compute the upper bound for the order of magnitude of entities and return the sum to add to avoid ID collision.
	 * 
	 * @return
	 */
	public static int collisionDelta() {
		int upper = (int) Math.log10(size.value * 2) + 1;
		return (int) Math.pow(10, upper);
	}


}

class Cache {
	int value = 0;
}
