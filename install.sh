#!/bin/sh
# TODO: turn this into an Ansilble role. :)

# create the applications, here we need three envs to promote the images between them
oc new-project sample-rest-app-dev
oc new-project sample-rest-app-stg
oc new-project sample-rest-app-prd

