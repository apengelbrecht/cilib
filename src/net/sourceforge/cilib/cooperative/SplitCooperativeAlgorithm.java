/*
 * CoOperativeOptimisationAglorithm.java
 *
 * Created on January 24, 2003, 11:44 AM
 *
 * 
 * Copyright (C) 2003 - 2006 
 * Computational Intelligence Research Group (CIRG@UP)
 * Department of Computer Science 
 * University of Pretoria
 * South Africa
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 * 
 */

package net.sourceforge.cilib.cooperative;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.cilib.algorithm.Algorithm;
import net.sourceforge.cilib.algorithm.InitialisationException;
import net.sourceforge.cilib.algorithm.MultiPopulationBasedAlgorithm;
import net.sourceforge.cilib.cooperative.contributionupdatestrategies.ContributionUpdateStrategy;
import net.sourceforge.cilib.cooperative.fitnessupdatestrategies.FitnessUpdateStrategy;
import net.sourceforge.cilib.cooperative.populationiterators.PopulationIterator;
import net.sourceforge.cilib.cooperative.splitstrategies.SplitStrategy;
import net.sourceforge.cilib.entity.Entity;
import net.sourceforge.cilib.problem.CooperativeOptimisationProblemAdapter;
import net.sourceforge.cilib.problem.Fitness;
import net.sourceforge.cilib.problem.OptimisationSolution;

/**
 * TODO test this class
 * This class forms that basis for any co-operative optimisation implementations.
 * Any algorithm that wishes to participate in a co-operative optimisation algorithm must implement the
 * {@link ParticipatingAlgorithm} interface. This class also implements {@link ParticipatingAlgorithm} meaning that
 * co-operative algorithms can be composed of co-operative algorithms again. 
 *
 * @author Edwin Peer
 * @author Theuns Cloete
 */
public class SplitCooperativeAlgorithm extends MultiPopulationBasedAlgorithm implements ParticipatingAlgorithm, Iterable<Algorithm> {
	protected CooperativeEntity context = null;
	protected SplitStrategy splitStrategy = null;
	protected PopulationIterator populationIterator = null;
	protected FitnessUpdateStrategy fitnessUpdateStrategy = null;
	protected ContributionUpdateStrategy contributionUpdateStrategy = null;
	protected boolean participated = false;

	/**
	 * Creates a new instance of a SplitCooperativeAglorithm
	 */
	public SplitCooperativeAlgorithm() {
		super();
		context = new CooperativeEntity();
	}
	
	public SplitCooperativeAlgorithm(SplitCooperativeAlgorithm copy) {
		
	}
	
	public SplitCooperativeAlgorithm clone() {
		return new SplitCooperativeAlgorithm(this);
	}
	
	public OptimisationSolution getBestSolution() {
		return new OptimisationSolution(optimisationProblem, context.get().clone());
	}

	public List<OptimisationSolution> getSolutions() {
		ArrayList<OptimisationSolution> solutions = new ArrayList<OptimisationSolution>(1);
		solutions.add(getBestSolution());
		return solutions;
	}

	public CooperativeEntity getContext() {
		return context;
	}

	public void setContext(CooperativeEntity c) {
		context = c;
	}

	public void setAlgorithm(Algorithm algorithm) {
		if(!(algorithm instanceof ParticipatingAlgorithm))
			throw new IllegalArgumentException("The given Algorithm is not a ParticipatingAlgorithm");
		populations.add(algorithm);
	}
	
	public int getNumberOfParticipants() {
		return populations.size();
	}

	public SplitStrategy getSplitStrategy() {
		return splitStrategy;
	}

	public void setSplitStrategy(SplitStrategy split) {
		splitStrategy = split;
	}

	public ContributionUpdateStrategy getContributionUpdateStrategy() {
		return contributionUpdateStrategy;
	}

	public void setContributionUpdateStrategy(ContributionUpdateStrategy contributionUpdate) {
		contributionUpdateStrategy = contributionUpdate;
	}

	public Entity getContribution() {
		return context;
	}

	public Fitness getContributionFitness() {
		return context.getFitness();
	}

	/**
	 * The purpose of this method should not be confused with the ContributionUpdateStrategy.
	 * This method sets the fitness for the context of the cooperating algorithm, i.e. the fitness
	 * for all cooperating algorithms as a whole.
	 */
	public void updateContributionFitness(Fitness fitness) {
		context.setFitness(fitness);
	}

	public Iterator<Algorithm> iterator() {
		if(populationIterator == null)
			throw new InitialisationException("The PopulationIterator has not been initialised yet.");
		return populationIterator.clone();
	}

	public Iterator getPopulationtIterator() {
		return populationIterator;
	}

	public void setPopulationIterator(PopulationIterator iterator) {
		if(populations == null)
			throw new InitialisationException("The populations (ArrayList<Algorithms>) have not been initialised yet.");
		populationIterator = iterator;
		populationIterator.setPopulations(populations);
	}

	public FitnessUpdateStrategy getFitnessUpdateStrategy() {
		return fitnessUpdateStrategy;
	}

	public void setFitnessUpdateStrategy(FitnessUpdateStrategy fitnessUpdate) {
		this.fitnessUpdateStrategy = fitnessUpdate;
	}

	public boolean participated() {
		return participated;
	}

	public void participated(boolean p) {
		participated = p;
	}

	public void resetParticipation(boolean participation) {
		for(Algorithm population : populations) {
			//TODO check whether this cast is safe
			((ParticipatingAlgorithm)population).participated(participation);
		}
	}

//	@Initialiser
//	QUESTION are initialisations (or initialisers) still deprecated? Should we use @Initialiser here instead?
	public void performInitialisation() {
		System.out.println(this);
		context.set(optimisationProblem.getDomain().getBuiltRepresenation().clone());
		splitStrategy.split(optimisationProblem, context, populations);
		context.reset();
		for(Algorithm participant : populations) {
			participant.performInitialisation();
			//TODO check whether this cast is safe
			context.append(((ParticipatingAlgorithm)participant).getContribution());
		}
	}

	public void performIteration() {
		for(Algorithm population : this) {
			population.performIteration();
			try {
				//TODO check whether this cast is safe
				CooperativeOptimisationProblemAdapter participantProblem = (CooperativeOptimisationProblemAdapter)((Algorithm)population).getOptimisationProblem();
				participantProblem.updateContext(context);
				contributionUpdateStrategy.updateContribution(((ParticipatingAlgorithm)population).getContribution(), 0, context, participantProblem.getOffset(), participantProblem.getDimension());
				fitnessUpdateStrategy.updateFitness(optimisationProblem, context);
				//TODO check whether this cast is safe
				((ParticipatingAlgorithm)population).participated(true);
			}
			catch(ClassCastException cce) {
				throw new InitialisationException("The population's problem is not a CooperativeOptimisationProblemAdapter");
			}
		}
		resetParticipation(false);
	}
}