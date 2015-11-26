package org.aksw.mandolin;

import java.util.TreeSet;

import org.aksw.mandolin.NameMapperProbKB.Type;
import org.aksw.mandolin.model.Cache;
import org.aksw.mandolin.model.ComparableLiteral;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Evidence {

	/**
	 * @param map
	 * @param SRC_PATH
	 * @param TGT_PATH
	 * @param LNK_PATH
	 * @param THR_MIN
	 * @param THR_MAX
	 * @param THR_STEP
	 */
	public static void build(final NameMapperProbKB map, final String SRC_PATH,
			final String TGT_PATH, final String LNK_PATH, final int THR_MIN,
			final int THR_MAX, final int THR_STEP) {

		// for similarity join
		final TreeSet<ComparableLiteral> setOfStrings = new TreeSet<>();
		final Cache cache = new Cache();

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

				// now check for non-instantiations...
				if (!p.equals(RDF.type.getURI())) {
					// it is supposed that the map contains only classes
					// and instances of these classes (see Classes.build)
					String relName = map.add(p, Type.RELATION);
					String subjName = map.getName(s);
					String objName = map.getName(o);

					// domain/range specification
					if (p.equals(RDFS.domain.getURI())) {
						subjName = map.add(s, Type.RELATION);
						// property name, target class, is domain
						map.addRelClass(subjName, objName, true);
					}
					if (p.equals(RDFS.range.getURI())) {
						subjName = map.add(s, Type.RELATION);
						// property name, target class, is range
						map.addRelClass(subjName, objName, false);
					}

					// if subject or object are not found, it means that they
					// have not been instantiated earlier (see Classes.build)
					if (subjName == null)
						// not found => instance subject, create entity
						subjName = map.add(s, Type.ENTITY);
					else {
						// create entity form for class
						if(subjName.startsWith(Type.CLASS.toString()))
							subjName = Type.ENTITY.toString() + "-" + subjName.substring(ProbKBData.CLS_LENGTH);
						// FIXME create stable entity form for properties 
						if(subjName.startsWith(Type.RELATION.toString()))
							subjName = Type.ENTITY.toString() + "-" + Integer.parseInt(subjName.substring(ProbKBData.REL_LENGTH)) + 10000;
						
					}
					if (objName == null)
						// not found => instance/datatype object, create entity
						objName = map.add(o, Type.ENTITY);
					else {
						// create entity form for class
						if(objName.startsWith(Type.CLASS.toString()))
							objName = Type.ENTITY.toString() + "-" + objName.substring(ProbKBData.CLS_LENGTH);
						// FIXME create stable entity form for properties
						if(objName.startsWith(Type.RELATION.toString()))
							objName = Type.ENTITY.toString() + "-" + Integer.parseInt(objName.substring(ProbKBData.REL_LENGTH)) + 10000;
					}

					// property, subject (entity), object (entity) names
					map.addRelationship(relName, subjName, objName);

				}

				if (arg0.getObject().isLiteral()) {
					String dtURI = arg0.getObject().getLiteralDatatypeURI();

					boolean considerString;
					if (dtURI == null)
						considerString = true;
					else
						considerString = dtURI.equals(XSD.xstring.getURI());

					if (considerString) {
						ComparableLiteral lit = new ComparableLiteral(arg0
								.getObject().getLiteral().toString(true), arg0
								.getObject().getLiteral().getValue().toString());
						setOfStrings.add(lit);
					}
				}

			}

		};

		RDFDataMgr.parse(dataStream, SRC_PATH);
		RDFDataMgr.parse(dataStream, TGT_PATH);
		RDFDataMgr.parse(dataStream, LNK_PATH);

		// call similarity join
		SimilarityJoin.build(map, setOfStrings, cache, THR_MIN, THR_MAX,
				THR_STEP);

	}

}
