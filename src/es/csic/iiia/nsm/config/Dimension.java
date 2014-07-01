package es.csic.iiia.nsm.config;

/**
 * Norm evaluation dimensions. The Norm Synthesis Machine evaluates norms in
 * terms of two dimensions: <i>effectiveness</i>, and <i>necessity</i>.
 * On the one hand, the effectiveness of a norm computes how effective the
 * norm is to avoid conflicts whenever agents comply with it. On the other
 * hand, the necessity of a norm evaluates if the norm is really necessary
 * to avoid conflicts. 
 * <p>
 * The NSM computes the effectiveness of a norm based on the conflicts that
 * arise after agents comply with the norm. Thus, if the agents comply with
 * the norm and no conflicts arise, then the NSM evaluates the norm as
 * <i>effective</i>. Otherwise, it evaluates the norm as
 * <i>ineffective</i> if conflicts arise after agents comply with it.
 * <p>
 * The necessity of a norm is computed based on the conflicts that arise
 * after agents infringe the norm. If agents infringe the norm and conflicts
 * arise, then the NSM evaluates the norm as <i>necessary</i>. By contrast,
 * if agents infringe the norm and no conflicts arise, the norm is evaluated
 * as <i>unnecessary</i> 
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 */
public enum Dimension {
	Effectiveness, Necessity;
}
