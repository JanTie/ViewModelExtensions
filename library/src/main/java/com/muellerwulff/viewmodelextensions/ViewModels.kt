/*
 * Copyright (c) 2018.  Müller & Wulff GmbH. All rights reserved.
 */

package com.muellerwulff.viewmodelextensions

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*

/**
 * Returns a [Lazy] delegate to access the ComponentActivity's ViewModel.
 *
 * It is meant to be used for [ViewModel]s which have a constructor that cannot be called by
 * [androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory]. The construction is done via the
 * [viewModelProducer].
 *
 * ```
 * class MyComponentActivity : ComponentActivity() {
 *     val viewmodel: MyViewModel by viewModelsCustom { MyViewModel(parameter1, parameter2) }
 * }
 * ```
 *
 * @param viewModelProducer a lambda returning an instance of [VM]
 */
@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.viewModelsCustom(
    noinline viewModelProducer: (() -> VM)
) = viewModels<VM> { SpecificFactory.create(viewModelProducer) }

/**
 * Returns a property delegate to access [ViewModel] by **default** scoped to this [Fragment].
 *
 * It is meant to be used for [ViewModel]s which have a constructor that cannot be called by
 * [androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory]. The construction is done via the
 * [viewModelProducer].
 *
 * ```
 * class MyFragment : Fragment() {
 *     val viewmodel: MyViewModel by viewModelsCustom { MyViewModel(parameter1, parameter2) }
 * }
 * ```
 *
 * Default scope may be overridden with parameter [ownerProducer]:
 * ```
 * class MyFragment : Fragment() {
 *     val viewmodel: MYViewModel by viewModelsCustom ({ requireParentFragment() }, { MyViewModel(parameter1, parameter2) })
 * }
 * ```
 *
 * This property can be accessed only after this Fragment is attached i.e., after
 * [Fragment.onAttach()], and access prior to that will result in IllegalArgumentException.
 *
 * @param ownerProducer a lambda returning a [ViewModelStoreOwner]
 * @param viewModelProducer a lambda returning an instance of [VM]
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModelsCustom(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline viewModelProducer: (() -> VM)
) = viewModels<VM>(
    { ownerProducer() },
    { SpecificFactory.create(viewModelProducer) })

/**
 * Returns a property delegate to access parent activity's [ViewModel].
 *
 * ```
 * class MyFragment : Fragment() {
 *     val viewmodel: MyViewModel by activityViewModelsCustom { MyViewModel(parameter1, parameter2) }
 * }
 * ```
 *
 * This property can be accessed only after this Fragment is attached i.e., after
 * [Fragment.onAttach()], and access prior to that will result in IllegalArgumentException.
 *
 * @param viewModelProducer a lambda returning an instance of [VM]
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModelsCustom(
    noinline viewModelProducer: (() -> VM)
) = activityViewModels<VM> { SpecificFactory.create(viewModelProducer) }


/**
 * [androidx.lifecycle.ViewModelProvider.Factory] which can only create [ViewModel]s of type [VM].
 * When trying to create a different type of [ViewModel], an IllegalStateException is thrown.
 *
 * @param producerClass the class, of which instance can be created
 * @param producer a lambda that returns an instance of [VM]
 */
@Suppress("UNCHECKED_CAST")
class SpecificFactory<VM : ViewModel>(
    private val producerClass: Class<VM>,
    private val producer: () -> VM
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (producerClass != modelClass) error("this factory can only create instances of $producerClass, $modelClass is not supported")
        return producer() as T
    }

    companion object {
        inline fun <reified VM : ViewModel> create(noinline producer: () -> VM) =
            SpecificFactory(VM::class.java, producer)
    }
}

//view model

/**
 * shorthand for [AndroidViewModel.getApplication]
 */
val AndroidViewModel.application: Application
    get() = getApplication()

/**
 * creates a [MutableLiveData] with a an initial value of [LiveData.getValue]
 */
fun <T> mutableLiveDataOf(default: T? = null) = MutableLiveData<T>(default)

/**
 * turns this [MutableLiveData] into a [LiveData]
 */
fun <T> MutableLiveData<T>.asLiveData(): LiveData<T> = this
