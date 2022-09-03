/**
 * This package provides a high-speed implementation of {@link org.vcssl.nano.vm.processor} package.
 * 
 * The outer-frame of the compiler provided by this package is {@link Accelerator} class,
 * and other classes are internal components.
 * 
 * This package contains far many classes than the "processor" package, and code in each classes is also more complicated.
 * Most of comments are the temporary note of/for the author of the class (written in Japanese).
 * Probably we will try to supplement/translate comments in this package in future, but it is undecided when it will be done.
 * 
 * Hence, we hardly recommend modifying code of classes in this package for now.
 * You can avoid to use this package in the script engine, by setting "ACCELERATOR_ENABLED" option to false.
 * If you do the above, all functions performed by this package will be switched to use only "processor" package.
 * The code of the classes in the "processor" package are far easy to read/modify than this package 
 * (though its processing speed is far slower than this package).
 */
package org.vcssl.nano.vm.accelerator;
