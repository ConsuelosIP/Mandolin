package org.aksw.mandolin.inference;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.aksw.mandolin.controller.NameMapper;
import org.aksw.mandolin.model.PredictionLiteral;
import org.aksw.mandolin.model.PredictionSet;

import com.googlecode.rockit.app.solver.pojo.Clause;
import com.googlecode.rockit.app.solver.pojo.Literal;
import com.googlecode.rockit.exception.ParseException;
import com.googlecode.rockit.exception.SolveException;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * Manager for the Gibbs-Sampling inference. Ground rules can be extracted from
 * the Postgre database after being generated by ProbKB (faster) or generated
 * through standard grounding by RockIt (slower).
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class ProbKBToRockitGibbsSampling extends RockitGibbsSampling {

	public static void main(String[] args) {

		PredictionSet ps = new ProbKBToRockitGibbsSampling(
				new NameMapper(OWL.sameAs.getURI())).infer();
		for (PredictionLiteral lit : ps)
			System.out.println(lit);

	}

	public ProbKBToRockitGibbsSampling(NameMapper map) {
		super(map);
	}

	/**
	 * Call ProbKB for grounding and preprocess its input for Gibbs sampling by
	 * RockIt.
	 */
	public PredictionSet infer() {

		Factors factors = Factors.getInstance();
		factors.preprocess(map.getAimName());

		// +++ STARTING POINTS +++
		// Prop2|alb|nob
		ArrayList<String> consistentStartingPoints = factors
				.getConsistentStartingPoints();

		// +++ CLAUSES +++
		// Clause [weight=0.0, restriction=[[Prop2|b|e]], hard=true]
		ArrayList<Clause> clauses = factors.getClauses();

		// +++ EVIDENCE +++
		// [Prop2|2db|h0e]
		Collection<Literal> evidence = factors.getEvidence();

		System.out.println(evidence);

		// call Gibbs sampler
		PredictionSet ps = null;
		try {
			ps = gibbsSampling(consistentStartingPoints, clauses, evidence);
		} catch (SQLException | SolveException | ParseException e) {
			e.printStackTrace();
		}

		return ps;
	}

}
