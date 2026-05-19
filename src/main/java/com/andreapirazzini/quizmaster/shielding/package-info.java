/**
 * Boundary dell'Exception Shielding. Tutti gli entry point passano da
 * {@link com.andreapirazzini.quizmaster.shielding.ExceptionShield#run(String, java.util.concurrent.Callable)}
 * così le eccezioni interne non arrivano mai all'utente verbatim.
 */
package com.andreapirazzini.quizmaster.shielding;
