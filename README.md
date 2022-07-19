# Adaptation of SVNKit for SCM-Manager

This reporitory is a clone of [SVNKit](http://svnkit.com/) with patches
for its usage in [SCM-Manager](https://scm-manager.org/). To distinguish the
patched versions from the original SVNKit versions, the patches

- change the group id to `sonia.svnkit`
- extend the versions with `-scm1`, `-scm2` and so on
- modify the distribution to use the [Nexus of the SCM-Manager project](https://packages.scm-manager.org/repository/releases/)

The patches can be found on specific branches, for example
`patch/1.10.3-scm2` for the second patch release of version `1.10.3`.

If you are looking for the original sources of SVNKit, you can find them
[here](https://svn.svnkit.com/repos/svnkit/).

This mirror is updated sporadically from the original sources. To get the patched
version before SVNKit 1.10.3, you have to use the old [patch repository](https://github.com/scm-manager/svnkit-patches)
and apply these patches manually.

## How to Update

To update this repository for a new release, you should

- Update the sources with the original source code (sadly, right now there does not seem
   to exist an up-to-date git mirror for SVNKit)
- create new tags for the new versions

## New Patch Version

To apply new patches to SVNKit and therefore prepare a new patch release, you should
differentiate between updates for existing patch releases and a new patch release for
a previously not adapted SVNKit release.

Do not take the following "instructions" too literally. There is no "one-fits-all"
process for patches and you may need to find an individual way!

### Update existing Patch Release

If you want to add a patch to, let's say version `1.10.3-scm3`, you should

- Check out the latest patch for your version:

```sh
git checkout patch/1.10.3-scm2
```

- Implement your changes and commit them.
- Increase the patch number in the versions
- Commit the changes

### Apply patches to new SVNKit Release

tbd

## Release

tbd
