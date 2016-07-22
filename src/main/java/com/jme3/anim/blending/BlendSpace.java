package com.jme3.anim.blending;

import com.jme3.animation.*;
import com.jme3.util.SafeArrayList;

import java.util.*;

/**
 * Created by Nehon on 05/07/2016.
 */
public interface BlendSpace {

    void blend (List<Anim> anims, BlendingDataPool weightedAnims, float globalWeight, float time);

}
