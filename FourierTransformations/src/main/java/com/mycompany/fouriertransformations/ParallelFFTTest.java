/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fouriertransformations;
import java.util.concurrent.CyclicBarrier ;
import java.awt.image.Raster ;
import java.awt.image.BufferedImage ;
import javax.imageio.ImageIO ;
import java.io.File ;
/**
 *
 * @author kamil
 */
public class ParallelFFTTest extends Thread {

    public static int N = 512 ; //Img size
    final static int P = 4; //Number of threads
    static CyclicBarrier barrier = new CyclicBarrier(P) ;
    static double [] [] X = new double [N] [N] ;
    static double [] [] CRe = new double [N] [N], CIm = new double [N] [N] ;
    static double [] [] reconRe = new double [N] [N], reconIm = new double [N] [N] ;

    public static void main(String [] args) throws Exception {

        // 1024p
        //BufferedImage img = ImageIO.read(new File("C:/Users/kamil/Documents/Scientific-Computing-and-Simulations/FourierTransformations/1024.png"));
        // 2048p
        //BufferedImage img = ImageIO.read(new File("C:/Users/kamil/Documents/Scientific-Computing-and-Simulations/FourierTransformations/2048.png"));
        // 4096p
        //BufferedImage img = ImageIO.read(new File("C:/Users/kamil/Documents/Scientific-Computing-and-Simulations/FourierTransformations/4096.png"));
        // Raster ras = img.getData() ;
        // for (int i = 0 ; i < N ; i++) {
        //     for (int j = 0 ; j < N ; j++) {
        //         X [i] [N-j-1] = ras.getSample(i, j, 0) ;
        //     }
        // }
        ReadPGM.read(X, "C:/Users/kamil/Documents/Scientific-Computing-and-Simulations/FourierTransformations/lena.pgm", N) ;

        DisplayDensity display =
                new DisplayDensity(X, N, "Original Image") ;

        // create array for in-place FFT, and copy original data to it
        for(int k = 0 ; k < N ; k++) {
            for(int l = 0 ; l < N ; l++) {
                CRe [k] [l] = X [k] [l] ;
            }
        }
        long startTime = System.currentTimeMillis();
        ParallelFFTTest [] threads = new ParallelFFTTest [P] ;
          for(int me = 0 ; me < P ; me++) {
              threads [me] = new ParallelFFTTest(me) ;
              threads [me].start() ;
          }
  
          for(int me = 0 ; me < P ; me++) {
              threads [me].join() ;
          }
        long endTime = System.currentTimeMillis();
        System.out.println("Calculation completed in " +
                             (endTime - startTime) + " milliseconds");
    }
    static void transpose(double [] [] a) {

        for(int i = 0 ; i < N ; i++) {
            for(int j = 0 ; j < i ; j++) {
                 //... swap values in a [i] [j] and a [j] [i] elements ...
                 double current = a[i][j];
                 a[i][j] = a[j][i];
                 a[j][i] = current;
            }
        }
    }
    
    static void fft2d(double [] [] re, double [] [] im, int isgn, int me, int begin, int end) {

        // For simplicity, assume square arrays

        //... fft1d on all rows of re, im ...
        for (int i = begin; i < end; i++) {
            FFT.fft1d(re[i], im[i], isgn);
        }
        synch();
        if(me == 0){
            transpose(re) ;
            transpose(im) ;
        }
        synch();
        //... fft1d on all rows of re, im ...
        for (int i = begin; i < end; i++) {
            FFT.fft1d(re[i], im[i], isgn);
        }
        synch();
        if(me == 0){
            transpose(re) ;
            transpose(im) ;
        }
        synch();
    }
    //... implementation of fft2d ...

    final static int B = N /P; //Block Size
    int me ;
    public ParallelFFTTest(int me) {
          this.me = me ;
      }

    public void run(){
        int begin = me * B;
        int end = begin + B;
        
        fft2d(CRe, CIm, 1, me, begin, end) ;  // Fourier transform
        synch();
        
        int cutoff = N/8 ;  // for example
        for(int k = 0 ; k < N ; k++) {
            int kSigned = k <= N/2 ? k : k - N ;
            for(int l = begin ; l < end ; l++) {
                int lSigned = l <= N/2 ? l : l - N ;
                if(Math.abs(kSigned) > cutoff || Math.abs(lSigned) > cutoff) {
                    CRe [k] [l] = 0 ;
                    CIm [k] [l] = 0 ;
                }
            }
        }
        synch();
        // if(me==0){
        //     Display2dFT display2a =
        //             new Display2dFT(CRe, CIm, N, "Truncated FT") ;
        // }
            // create array for in-place inverse FFT, and copy FT to it
            
            for(int k = 0 ; k < N ; k++) {
                for(int l = begin ; l < end ; l++) {
                    reconRe [k] [l] = CRe [k] [l] ;
                    reconIm [k] [l] = CIm [k] [l] ;
                }
            }
        
        synch();
        fft2d(reconRe, reconIm, -1, me, begin, end) ;  // Inverse Fourier transform
        //  if(me==0){
        //     DisplayDensity display3 =
        //             new DisplayDensity(reconRe, N, "Reconstructed Image") ;
        // }
    }

    static void synch() {
        try {
            barrier.await() ;
        }
        catch(Exception e) {
            e.printStackTrace() ;
            System.exit(1) ;
        }
    }
}
