package io.github.apace100.origins.util;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

public enum Space {
    WORLD, LOCAL, LOCAL_HORIZONTAL, VELOCITY, VELOCITY_NORMALIZED, VELOCITY_HORIZONTAL, VELOCITY_HORIZONTAL_NORMALIZED;

    public static void rotateVectorToBase(Vec3d newBase, Vector3f vector) {
        Vec3d globalForward = new Vec3d(0, 0, 1);
        Vec3d v = globalForward.crossProduct(newBase).normalize();
        double c = Math.acos(globalForward.dotProduct(newBase));
        Quaternion quat = new Quaternion(new Vector3f((float)v.x, (float)v.y, (float)v.z), (float)c, false);
        vector.rotate(quat);
    }
}
