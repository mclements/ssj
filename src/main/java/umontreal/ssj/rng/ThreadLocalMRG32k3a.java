
/* *
 * Title:        <p>
 * Description:  <p>
 * Copyright:    Copyright (c) <p>
 * Company:      <p>
 * @author
 * @version 1.0
 */
package umontreal.ssj.rng;

import java.io.Serializable;
import umontreal.ssj.util.PrintfFormat;
import umontreal.ssj.util.ArithmeticMod;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Extends the abstract class {@link RandomStreamBase} by using as a
 * backbone (or main) generator the combined multiple recursive
 * generator (CMRG) <TT>MRG32k3a</TT> proposed by L'Ecuyer,
 * implemented in 64-bit floating-point arithmetic.
 * This backbone generator has a period length of
 *   
 * <SPAN CLASS="MATH"><I>&#961;</I> = 2<SUP>191</SUP></SPAN>. 
 * The values of <SPAN CLASS="MATH"><I>V</I></SPAN>, <SPAN CLASS="MATH"><I>W</I></SPAN>, and <SPAN CLASS="MATH"><I>Z</I></SPAN> are <SPAN CLASS="MATH">2<SUP>51</SUP></SPAN>, <SPAN CLASS="MATH">2<SUP>76</SUP></SPAN>, and <SPAN CLASS="MATH">2<SUP>127</SUP></SPAN>,
 * respectively. (See {@link RandomStream} for their definition.)
 * The seed of the RNG, and the state of a stream at any given step,
 * are six-dimensional vectors of 32-bit integers, stored in <TT>double</TT>.
 * The default initial seed of the RNG is
 * 
 * <SPAN CLASS="MATH">(12345, 12345, 12345, 12345, 12345, 12345)</SPAN>.
 * 
 */
public final class ThreadLocalMRG32k3a extends RandomStreamBase {

    private static final long serialVersionUID = 70510L;
    
    private static final double m1 = 4294967087.0;
    private static final double m2 = 4294944443.0;
    private static final double a12 = 1403580.0;
    private static final double a13n = 810728.0;
    private static final double a21 = 527612.0;
    private static final double a23n = 1370589.0;
    private static final double norm = 2.328306549295727688e-10;
    
    
    private static final double A1p76[][] = {
        {82758667.0, 1871391091.0, 4127413238.0},
        {3672831523.0, 69195019.0, 1871391091.0},
        {3672091415.0, 3528743235.0, 69195019.0}
    };
    private static final double A2p76[][] = {
        {1511326704.0, 3759209742.0, 1610795712.0},
        {4292754251.0, 1511326704.0, 3889917532.0},
        {3859662829.0, 4292754251.0, 3708466080.0}
    };
    private static final double A1p127[][] = {
            {    2427906178.0, 3580155704.0,  949770784.0 },
            {     226153695.0, 1230515664.0, 3580155704.0 },
            {    1988835001.0,  986791581.0, 1230515664.0 }
    };
    private static final double A2p127[][] = {
            {    1464411153.0,  277697599.0, 1610723613.0 },
            {      32183930.0, 1464411153.0, 1022607788.0 },
            {    2824425944.0,   32183930.0, 2093834863.0 }
    };
    
    
    /** Current state of the stream. 
        We do not make use of nextSeed. */
    private double Cg[] = {12345, 12345, 12345,
        12345, 12345, 12345};
    private double Bg[] = {12345, 12345, 12345,
        12345, 12345, 12345};
    private double Ig[] = {12345, 12345, 12345,
        12345, 12345, 12345};
    
    /** Thread local reference. */
    private static final ThreadLocal<ThreadLocalMRG32k3a> localRandom =
            new ThreadLocal<ThreadLocalMRG32k3a>() {

                @Override
                protected ThreadLocalMRG32k3a initialValue() {
                    return new ThreadLocalMRG32k3a();
                }
            };
    
    /** Tweak to generate a custom and mastered thread id 
     */
    private static final AtomicInteger nextId = new AtomicInteger(0);
    
    /** Thread local variable containing each thread's ID.
     Custom thread id since Java' Thread.currentThread().getId() is 
     * reproducible through runs and thus not reliable for our concerns.
     */
    public static final ThreadLocal<Integer> threadId =
            new ThreadLocal<Integer>() {

                @Override
                protected Integer initialValue() {
                    return nextId.getAndIncrement();
                }
            };

    /**
     * Constructs a new stream, initializes its seed <SPAN CLASS="MATH"><I>I</I><SUB>g</SUB></SPAN>,
     *    sets <SPAN CLASS="MATH"><I>B</I><SUB>g</SUB></SPAN> and <SPAN CLASS="MATH"><I>C</I><SUB>g</SUB></SPAN> equal to <SPAN CLASS="MATH"><I>I</I><SUB>g</SUB></SPAN>, and sets its antithetic switch
     *    to <TT>false</TT>.
     *    The seed <SPAN CLASS="MATH"><I>I</I><SUB>g</SUB></SPAN> is equal to the initial seed of the package given by
     *    {@link #setPackageSeed(long[]) setPackageSeed} if this is the first stream created,
     *    otherwise it is <SPAN CLASS="MATH"><I>Z</I></SPAN> steps ahead of that of the stream most recently
     *    created in this class.
     * 
     */
    ThreadLocalMRG32k3a() {
        // perform a jump ahead with ThreadId
        this.jumpAhead(threadId.get());
    }

    /** Jump Ahead of N streams */
    private void jumpAhead(int pow) {

        /** A1^N temporary matrix */
        double A1_pN[][] = {
            {0.0, 1.0, 0.0},
            {0.0, 0.0, 1.0},
            {-810728.0, 1403580.0, 0.0}
        };

        /** A2^N temporary matrix */
        double A2_pN[][] = {
            {0.0, 1.0, 0.0},
            {0.0, 0.0, 1.0},
            {-1370589.0, 0.0, 527612.0}
        };


        ArithmeticMod.matPowModM(A1p127, A1_pN, m1, pow); // (A1^(2^76))^n mod m
        ArithmeticMod.matPowModM(A2p127, A2_pN, m2, pow); // (A2^(2^76))^n mod m

        matVecModM_low(A1_pN, Ig, Ig, m1);
        matVecModM_high(A2_pN, Ig, Ig, m2);

	resetStartStream();
	
    }

    public void resetNextStream() {
	    jumpAhead(1);
    }
	
    /**  */
    public void resetNextSubstream() {

        /** A1^N temporary matrix */
        double A1_pN[][] = {
            {0.0, 1.0, 0.0},
            {0.0, 0.0, 1.0},
            {-810728.0, 1403580.0, 0.0}
        };

        /** A2^N temporary matrix */
        double A2_pN[][] = {
            {0.0, 1.0, 0.0},
            {0.0, 0.0, 1.0},
            {-1370589.0, 0.0, 527612.0}
        };

        matVecModM_low(A1p76, Bg, Bg, m1);
        matVecModM_high(A2p76, Bg, Bg, m2);

	resetStartSubstream();
	
    }

	
   public void resetStartStream()  {
      for (int i = 0; i < 6;  ++i)
         Bg[i] = Ig[i];
      resetStartSubstream();
   }

   public void resetStartSubstream()  {
      for (int i = 0; i < 6;  ++i)
         Cg[i] = Bg[i];
   }

	
    /**
     * Computes the result of 
     * <SPAN CLASS="MATH"><TT>A</TT>&#215;<B>s</B> mod <I>m</I></SPAN> and puts the
     *   result in <TT>v</TT>. Where <TT>s</TT> and <TT>v</TT> are both column vectors. This 
     *   method works even if <TT>s</TT> = <TT>v</TT>.
     * 
     * (used in the first part of JumpAhead)
     * 
     * @param A the multiplication matrix
     * 
     *   @param s the multiplied vector
     * 
     *   @param v the result of the multiplication
     * 
     *   @param m the modulus
     * 
     */
    private static void matVecModM_high(double A[][], double s[], double v[],
            double m) {
        int i;
        double x[] = new double[3]; // Necessary if v = s

        for (i = 0; i < 3; ++i) {
            x[i] = ArithmeticMod.multModM(A[i][0], s[3], 0.0, m);
            x[i] = ArithmeticMod.multModM(A[i][1], s[4], x[i], m);
            x[i] = ArithmeticMod.multModM(A[i][2], s[5], x[i], m);
        }
        for (i = 0; i < 3; ++i) {
            v[i + 3] = x[i];
        }
    }

    /**
     * Computes the result of 
     * <SPAN CLASS="MATH"><TT>A</TT>&#215;<B>s</B> mod <I>m</I></SPAN> and puts the
     *   result in <TT>v</TT>. Where <TT>s</TT> and <TT>v</TT> are both column vectors. This 
     *   method works even if <TT>s</TT> = <TT>v</TT>.
     * 
     *  (used in the second part of JumpAhead)
     * 
     * @param A the multiplication matrix
     * 
     *   @param s the multiplied vector
     * 
     *   @param v the result of the multiplication
     * 
     *   @param m the modulus
     * 
     */
    private static void matVecModM_low(double A[][], double s[], double v[],
            double m) {
        int i;
        double x[] = new double[3]; // Necessary if v = s

        for (i = 0; i < 3; ++i) {
            x[i] = ArithmeticMod.multModM(A[i][0], s[0], 0.0, m);
            x[i] = ArithmeticMod.multModM(A[i][1], s[1], x[i], m);
            x[i] = ArithmeticMod.multModM(A[i][2], s[2], x[i], m);
        }
        for (i = 0; i < 3; ++i) {
            v[i] = x[i];
        }
    }

    /**
     * Returns the current thread's {@code MRG32k3a}.
     *
     * @return the current thread's {@code MRG32k3a}
     */
    public static ThreadLocalMRG32k3a current() {
        return localRandom.get();
    }

    /**
     * Sets the initial seed <SPAN CLASS="MATH"><I>I</I><SUB>g</SUB></SPAN> of this stream
     *   to the vector <TT>seed[0..5]</TT>.  This vector must satisfy the same
     *   conditions as in <TT>setPackageSeed</TT>.
     *   The stream is then reset to this initial seed.
     *   The states and seeds of the other streams are not modified.
     *   As a result, after calling this method, the initial seeds
     *   of the streams are no longer spaced <SPAN CLASS="MATH"><I>Z</I></SPAN> values apart.
     *   For this reason, <SPAN  CLASS="textit">this method should be used only in very
     *   exceptional situations</SPAN> (I have never used it myself!);
     *   proper use of <TT>reset...</TT>
     *   and of the stream constructor is preferable.
     *  
     * @param seed array of 6 integers representing the new seed
     * 
     * 
     */
    //@Override
    public void setSeed(long seed) {
        // disabled
    }

    //@Override
    public double next() {
        int k;
        double p1, p2;
        
        /* Component 1 */
        p1 = a12 * Cg[1] - a13n * Cg[0];
        k = (int) (p1 / m1);
        p1 -= k * m1;
        if (p1 < 0.0) {
            p1 += m1;
        }
        Cg[0] = Cg[1];
        Cg[1] = Cg[2];
        Cg[2] = p1;
        
        /* Component 2 */
        p2 = a21 * Cg[5] - a23n * Cg[3];
        k = (int) (p2 / m2);
        p2 -= k * m2;
        if (p2 < 0.0) {
            p2 += m2;
        }
        Cg[3] = Cg[4];
        Cg[4] = Cg[5];
        Cg[5] = p2;
        
        /* Combination */
        return ((p1 > p2) ? (p1 - p2) * norm : (p1 - p2 + m1) * norm);
    }

   /**
    * Returns a string containing the name and the current state @f$C_g@f$
    * of this stream.
    *  @return the state of the generator, formated as a string
    */
   public String toString() {
      PrintfFormat str = new PrintfFormat();

      str.append ("The current state of the MRG32k3a");
      if (name != null && name.length() > 0)
         str.append (" " + name);
      str.append (":" + PrintfFormat.NEWLINE + "   Cg = { ");
      str.append ((long) Cg[0] + ", ");
      str.append ((long) Cg[1] + ", ");
      str.append ((long) Cg[2] + ", ");
      str.append ((long) Cg[3] + ", ");
      str.append ((long) Cg[4] + ", ");
      str.append ((long) Cg[5] + " }" + PrintfFormat.NEWLINE +
          PrintfFormat.NEWLINE);

      return str.toString();
   }

	protected double nextValue() {
		return next();
	}

   private static void validateSeed (long seed[]) {
      if (seed.length < 6)
         throw new IllegalArgumentException ("Seed must contain 6 values");
      if (seed[0] == 0 && seed[1] == 0 && seed[2] == 0)
         throw new IllegalArgumentException
            ("The first 3 values must not be 0");
      if (seed[3] == 0 && seed[4] == 0 && seed[5] == 0)
         throw new IllegalArgumentException
            ("The last 3 values must not be 0");
      final long m1 = 4294967087L;
      if (seed[0] >= m1 || seed[1] >= m1 || seed[2] >= m1)
         throw new IllegalArgumentException
            ("The first 3 values must be less than " + m1);
      final long m2 = 4294944443L;
      if (seed[3] >= m2 || seed[4] >= m2 || seed[5] >= m2)
         throw new IllegalArgumentException
            ("The last 3 values must be less than " + m2);
   }

	
}
